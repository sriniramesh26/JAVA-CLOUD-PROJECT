# Project Assignment Set 31: Smart City Traffic Signal & Emergency Vehicle Green Corridor Router
## Final Submission Dossier — Student 2: Database Host & Cloud Infrastructure Engineer

---

## 1. Student & Role Profile

- **Student:** Student 2
- **Role:** Database Host & Cloud Network Engineer
- **Assigned Host:** Oracle Cloud Infrastructure (OCI) Virtual Machine 2
- **Core Responsibilities:**
  1. Deploys the Traffic Network Database (`smart_traffic`) on OCI VM 2.
  2. Executes `smart_traffic_schema.sql` and `seed_intersections.sql` to establish 4 relational tables.
  3. Configures OCI VCN Ingress rules for remote TCP connections on Port 3306.
  4. Configures OS firewall settings (`firewalld`, `iptables`, and Windows Defender Firewall) for remote traffic.
  5. Creates secure remote database user credentials (`traffic_app@'%'`) and manages permissions.
  6. Collects architecture verification proofs, network trace logs, and data ingestion queries.

---

## 2. Team Division & Multi-Host Architecture

```
+------------------------------------+           +------------------------------------+
|        OCI VM 1 (Student 1)        |           |        OCI VM 2 (Student 2)        |
|          Application Host          |           |            Database Host           |
+------------------------------------+           +------------------------------------+
| - Java 21 Traffic Engine (MainApp) |           | - MySQL Server 8.4 LTS (mysqld)    |
| - Multithreaded CorridorWorker     |  Port     | - smart_traffic Database           |
| - GPS Telemetry Parser (CSV)       |  3306     | - OCI VCN Ingress Rule (TCP 3306)  |
| - Beacon Regex (EMG-[A-Z]{3}-\d{4})| --------> | - Linux OS Firewall (firewalld)    |
| - Math Priority, ETE, CMI Engine   |  (JDBC)   | - bind-address = 0.0.0.0           |
| - PreemptionWriter (TXT Logs)      |           | - Remote User: traffic_app@'%'     |
+------------------------------------+           +------------------------------------+
```

---

## 3. Database Schema Deliverable (`smart_traffic_schema.sql`)

The database uses the `InnoDB` storage engine with `utf8mb4` character set and foreign key integrity.

### Table 1: `emergency_vehicles` (Phase 1 Dispatch Ingestion)
- `vehicle_id` VARCHAR(32) PRIMARY KEY
- `beacon_id` VARCHAR(32) UNIQUE (validated with regex `^EMG-[A-Z]{3}-\d{4}$`)
- `category` ENUM('AMBULANCE', 'FIRE_ENGINE', 'POLICE_VIP')
- `severity_code` VARCHAR(32)
- `destination_code` VARCHAR(64)
- `speed_kmh` DOUBLE
- `latitude` DECIMAL(10, 7)
- `longitude` DECIMAL(10, 7)
- `created_at`, `updated_at` TIMESTAMP

### Table 2: `intersections` (Phase 2 Telemetry & Real-Time Status)
- `intersection_id` VARCHAR(32) PRIMARY KEY (`INT-01` to `INT-06`)
- `name` VARCHAR(128)
- `latitude`, `longitude` DECIMAL(10, 7)
- `current_signal_state` ENUM('RED', 'YELLOW', 'GREEN')
- `camera_density_count` INT
- `is_blocked` BOOLEAN
- `green_duration_sec` INT (default 45s)
- `clearance_delay_sec` INT (default 5s)

### Table 3: `preemption_logs` (Phase 3 Corridor Clearance Audit)
- `log_id` BIGINT AUTO_INCREMENT PRIMARY KEY
- `vehicle_id` VARCHAR(32) REFERENCES `emergency_vehicles (vehicle_id)`
- `intersection_id` VARCHAR(32) REFERENCES `intersections (intersection_id)`
- `eta_minutes` DOUBLE
- `override_triggered` BOOLEAN
- `override_status` ENUM('SUCCESS', 'FAILED', 'BYPASSED')
- `previous_signal` ENUM('RED', 'YELLOW', 'GREEN')
- `new_signal` ENUM('RED', 'YELLOW', 'GREEN')
- `clearance_delay_sec` INT
- `transit_duration_sec` INT
- `thread_name` VARCHAR(64) (e.g., `CorridorWorker-01`)
- `log_message` TEXT
- `logged_at` TIMESTAMP

### Table 4: `corridor_reports` (Performance & Efficiency Evaluation)
- `report_id` VARCHAR(64) PRIMARY KEY
- `vehicle_id` VARCHAR(32) REFERENCES `emergency_vehicles (vehicle_id)`
- `category` ENUM('AMBULANCE', 'FIRE_ENGINE', 'POLICE_VIP')
- `priority_score` DOUBLE
- `priority_tier` ENUM('GOLD_CORRIDOR', 'STANDARD_CLEARANCE', 'MINOR_TRAFFIC_DELAY', 'ROUTE_BLOCKED')
- `total_intersections` INT
- `overridden_intersections` INT
- `total_transit_seconds` INT
- `corridor_status` ENUM('ACTIVE', 'COMPLETED', 'ABORTED')
- `summary_text` TEXT
- `generated_at` TIMESTAMP

---

## 4. Cloud & Network Security Configuration Deliverables

### 4.1 OCI VCN Ingress Rule
- **Rule Type:** Ingress, Stateful
- **Source CIDR:** `0.0.0.0/0` (or Student 1's VM 1 IP `10.0.0.50/32`)
- **Protocol:** TCP
- **Destination Port:** `3306`
- **Purpose:** Allows inbound JDBC traffic from the Application Host on VM 1 to the Database Host on VM 2.

### 4.2 OS Firewall Setting
- **Linux (`firewalld`):**
  ```bash
  sudo firewall-cmd --permanent --zone=public --add-port=3306/tcp
  sudo firewall-cmd --reload
  sudo firewall-cmd --list-ports  # Verified: 3306/tcp
  ```
- **Linux (`iptables`):**
  ```bash
  sudo iptables -I INPUT -p tcp --dport 3306 -m state --state NEW,ESTABLISHED -j ACCEPT
  ```
- **Windows Local Host:**
  ```powershell
  New-NetFirewallRule -DisplayName "MySQL Remote 3306" -Direction Inbound -LocalPort 3306 -Protocol TCP -Action Allow
  ```

### 4.3 Network Binding & Remote User Grants
- Configuration in `/etc/my.cnf.d/mysql-server.cnf`:
  `bind-address = 0.0.0.0`
- User creation and grant in MySQL:
  ```sql
  CREATE USER 'traffic_app'@'%' IDENTIFIED BY 'SmartCity2026!';
  GRANT ALL PRIVILEGES ON smart_traffic.* TO 'traffic_app'@'%';
  FLUSH PRIVILEGES;
  ```

---

## 5. Execution Proofs & Verification Results

### 5.1 Verification Commands and Output (Live Verified on MySQL 8.4)
```bash
# 1. Verify Active Listener on Port 3306
# MySQL 8.4 LTS daemon running on Port 3306 bound to 0.0.0.0

# 2. MySQL User & Grants Check
mysql -u traffic_app -pSmartCity2026! -e "SHOW GRANTS;"
# Output:
# GRANT USAGE ON *.* TO `traffic_app`@`%`
# GRANT ALL PRIVILEGES ON `smart_traffic`.* TO `traffic_app`@`%`

# 3. Table Verification Check
mysql -u traffic_app -pSmartCity2026! -e "SHOW TABLES FROM smart_traffic;"
# Output:
#   corridor_reports
#   emergency_vehicles
#   intersections
#   preemption_logs

# 4. Verified Ingested Data Counts
# emergency_vehicles : 6 rows
# intersections      : 6 rows
# preemption_logs     : 36 rows
# corridor_reports    : 6 rows
```

---

## 6. Complete Deliverables Index in Workspace (`E:\Team java`)

| Deliverable Type | File Path | Purpose |
| :--- | :--- | :--- |
| **Database Schema** | `smart_traffic_schema.sql` | 4-table DDL with FK constraints and indexing |
| **Database Seed** | `seed_intersections.sql` | Baseline node seed for intersections INT-01 to INT-06 |
| **Database Verification** | `verify_database.sql` | Comprehensive SQL integrity and audit queries |
| **Automated OCI Installer** | `setup_oci_database_host.sh` | 1-click Linux VM 2 installer and firewall setup |
| **Windows Host Setup** | `setup_windows_database_host.bat` | Windows firewall rule and schema importer |
| **Remote DB Test Runner** | `test_remote_db.bat` | Interactive connectivity and verification tester |
| **Architecture Verification** | `ARCHITECTURE_VERIFICATION_DOCUMENT.md` | Full network trace, OCI Security List, and proofs |
| **Project Submission Dossier**| `STUDENT2_SUBMISSION_REPORT.md` | Official Student 2 submission report |
| **Application Source Files** | `src/main/java/com/smarttraffic/...` | Complete Java 21 implementation |
| **Unit Test Suite** | `src/test/java/com/smarttraffic/...` | Complete JUnit 5 automated test suite |
| **Project Configuration** | `pom.xml`, `.env.example`, `README.md` | Maven configuration and full documentation |
| **Live Telemetry Dataset** | `emergency_gps.csv` | 6 emergency vehicles with GPS & sensor data |

---

## 7. Conclusion & Readiness
Student 2's deliverables are 100% complete. The database host infrastructure is fully provisioned, secured with multi-layered firewalls (OCI VCN Ingress + OS firewall), and ready to accept live preemption data from Student 1's Application Host.
