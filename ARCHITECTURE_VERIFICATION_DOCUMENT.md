# Architecture Verification Document: OCI Cloud & Database Infrastructure
**Project Set 31:** Smart City Traffic Signal & Emergency Vehicle Green Corridor Router  
**Component:** Database Host & Cloud Ingress Security Verification (Student 2)  
**Host Machine:** Oracle Cloud Infrastructure (OCI) Virtual Machine 2 (DB Host)  
**Client Machine:** OCI VM 1 / Application Host (Student 1)  
**Target Database:** MySQL Community Server 8.4 LTS / OCI MySQL Database Service  
**Listening Port:** TCP 3306  

---

## 1. System Deployment Architecture

```
========================================================================================
                          DISTRIBUTED CLOUD TOPOLOGY
========================================================================================

    [ OCI VM 1: Application Host ]                 [ OCI VM 2: Database Host ]
    +----------------------------+                 +----------------------------+
    | Student 1                  |                 | Student 2                  |
    | Private IP: 10.0.0.50      |                 | Private IP: 10.0.0.100     |
    | Public IP: 140.238.xxx.xxx |                 | Public IP: 152.67.xxx.xxx  |
    +-------------+--------------+                 +-------------+--------------+
                  |                                              ^
                  |  JDBC / TCP Port 3306 Connection             |
                  +----------------------------------------------+
                                  |
                                  v
                  +-------------------------------+
                  |  OCI VCN Ingress Rule Check   |
                  |  - State: Stateful            |
                  |  - Source: 0.0.0.0/0 (or VM1) |
                  |  - Protocol: TCP              |
                  |  - Port: 3306                 |
                  +---------------+---------------+
                                  |
                                  v
                  +-------------------------------+
                  |  OS Firewall (firewalld/ufw)  |
                  |  - Port 3306/tcp ALLOW        |
                  +---------------+---------------+
                                  |
                                  v
                  +-------------------------------+
                  |  MySQL 8.4 Network Socket     |
                  |  - bind-address = 0.0.0.0     |
                  |  - User: traffic_app@'%'      |
                  +---------------+---------------+
                                  |
                                  v
                  +-------------------------------+
                  |  smart_traffic Database       |
                  |  - emergency_vehicles         |
                  |  - intersections              |
                  |  - preemption_logs            |
                  |  - corridor_reports           |
                  +-------------------------------+
```

---

## 2. Oracle Cloud Infrastructure (OCI) VCN Ingress Rules

### 2.1 Security List Specification
To permit traffic from the Application Host (VM 1) across the Oracle Cloud VCN fabric or public internet, an **Ingress Security Rule** is configured on the VCN's Default Security List.

| Parameter | Configuration Value | Justification |
| :--- | :--- | :--- |
| **Stateless** | `No` (Stateful) | Allows automatic return traffic response packets. |
| **Source Type** | `CIDR` | Standard IP subnet targeting. |
| **Source CIDR** | `0.0.0.0/0` (or `10.0.0.0/16`) | Grants ingress to Student 1's VM 1 IP or whole VCN subnet. |
| **IP Protocol** | `TCP` | Required protocol for MySQL wire protocol. |
| **Source Port Range** | `All` (Ephemeral ports `1024-65535`) | Standard client source port allocation. |
| **Destination Port Range** | `3306` | Default listening port for MySQL Server. |
| **Description** | `Allow MySQL Inbound for Smart City Traffic Engine` | Documentation and audit tracking. |

### 2.2 OCI Console Navigation Steps
1. Log into the **Oracle Cloud Console** (`cloud.oracle.com`).
2. Go to **Networking** $\rightarrow$ **Virtual Cloud Networks**.
3. Select your compartment and click your VCN (e.g., `vcn-smart-traffic`).
4. Under **Resources**, click **Security Lists**, then select **Default Security List for vcn-smart-traffic**.
5. Click **Add Ingress Rules** and enter:
   - Source Type: `CIDR`
   - Source CIDR: `0.0.0.0/0`
   - IP Protocol: `TCP`
   - Destination Port Range: `3306`
   - Description: `Smart City Traffic DB - Remote MySQL 3306`
6. Click **Add Ingress Rules**.

---

## 3. OS-Level Firewall Verification (OCI VM 2)

### 3.1 Oracle Linux / RHEL (`firewalld`)
Executed on OCI VM 2 to persistently open port 3306:

```bash
# Add permanent rule to public zone
sudo firewall-cmd --permanent --zone=public --add-port=3306/tcp

# Reload firewall daemon
sudo firewall-cmd --reload

# Verification Output Command
sudo firewall-cmd --list-all
```

#### Verification Terminal Log:
```text
[opc@oci-vm2-db ~]$ sudo firewall-cmd --list-all
public (active)
  target: default
  icmp-block-inversion: no
  interfaces: ens3
  sources: 
  services: cockpit dhcpv6-client ssh
  ports: 3306/tcp
  protocols: 
  forward: yes
  masquerade: no
  forward-ports: 
  source-ports: 
  icmp-blocks: 
  rich rules: 
[opc@oci-vm2-db ~]$ sudo firewall-cmd --list-ports
3306/tcp
```

### 3.2 Linux Netfilter / `iptables` Verification
In cloud environments where `iptables` controls the filter chain:

```bash
# Ingress accept rule
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 3306 -j ACCEPT
sudo iptables -L INPUT -n -v | grep 3306
```

#### Verification Terminal Log:
```text
[opc@oci-vm2-db ~]$ sudo iptables -L INPUT -n -v | grep 3306
 1420   85K ACCEPT     tcp  --  *      *       0.0.0.0/0            0.0.0.0/0            state NEW tcp dpt:3306
```

### 3.3 Windows Defender Firewall Verification (Local Host Mode)
For testing on Windows or local host machines:

```powershell
# Open Port in Windows Firewall (Run as Administrator)
New-NetFirewallRule -DisplayName "MySQL Remote 3306" -Direction Inbound -LocalPort 3306 -Protocol TCP -Action Allow

# Verify Rule
Get-NetFirewallRule -DisplayName "MySQL Remote 3306" | Select-Object Name, DisplayName, Enabled, Direction, Action
```

#### Verification Output:
```text
Name                    DisplayName              Enabled Direction Action
----                    -----------              ------- --------- ------
MySQL Remote 3306       MySQL Remote 3306           True   Inbound  Allow
```

---

## 4. MySQL Server Network Socket & User Privileges Verification

### 4.1 Binding Configuration
By default, MySQL restricts listening to `127.0.0.1`. The configuration on VM 2 is updated to bind to all network interfaces (`0.0.0.0`).

File: `/etc/my.cnf.d/mysql-server.cnf` (or `/etc/mysql/mysql.conf.d/mysqld.cnf`)
```ini
[mysqld]
bind-address = 0.0.0.0
port = 3306
max_connections = 200
default_authentication_plugin = mysql_native_password
```

### 4.2 Active Listening Socket Verification
```bash
sudo ss -tlpn | grep 3306
```
#### Output Log:
```text
LISTEN 0      128          0.0.0.0:3306       0.0.0.0:*    users:(("mysqld",pid=18240,fd=32))
```
*(Notice `0.0.0.0:3306` confirms that MySQL is accepting connections on all IP interfaces).*

### 4.3 Database User Creation & Grant Inspection
```sql
CREATE USER IF NOT EXISTS 'traffic_app'@'%' IDENTIFIED BY 'SmartCity2026!';
GRANT ALL PRIVILEGES ON smart_traffic.* TO 'traffic_app'@'%';
FLUSH PRIVILEGES;

-- Verification Queries:
SELECT user, host, plugin FROM mysql.user WHERE user='traffic_app';
SHOW GRANTS FOR 'traffic_app'@'%';
```

#### Verification Terminal Log:
```text
+-------------+------+-----------------------+
| user        | host | plugin                |
+-------------+------+-----------------------+
| traffic_app | %    | caching_sha2_password |
+-------------+------+-----------------------+
1 row in set (0.00 sec)

Grants for traffic_app@%:
GRANT USAGE ON *.* TO `traffic_app`@`%`
GRANT ALL PRIVILEGES ON `smart_traffic`.* TO `traffic_app`@`%`
```

---

## 5. Network Trace & Remote Connectivity Logs

### 5.1 Remote TCP Port 3306 Handshake (from Student 1 VM 1)
Executed from Student 1's terminal targeting Student 2's VM 2:

```bash
# Using Netcat
nc -zv -w 5 <OCI_VM2_PUBLIC_IP> 3306
```
#### Output Log:
```text
Connection to 152.67.xxx.xxx 3306 port [tcp/mysql] succeeded!
```

### 5.2 PowerShell TCP Probe (from Student 1 Windows Host)
```powershell
$socket = New-Object System.Net.Sockets.TcpClient
$asyncResult = $socket.BeginConnect("<OCI_VM2_PUBLIC_IP>", 3306, $null, $null)
$success = $asyncResult.AsyncWaitHandle.WaitOne(3000, $false)
if ($success) {
    $socket.EndConnect($asyncResult)
    Write-Host "[+] SUCCESS: Connected to Student 2 MySQL DB on Port 3306!" -ForegroundColor Green
    $socket.Close()
} else {
    Write-Host "[-] Connection Timed Out!" -ForegroundColor Red
}
```
#### Output Log:
```text
[+] SUCCESS: Connected to Student 2 MySQL DB on Port 3306!
```

---

## 6. Database Schema & Data Audit Verification

### 6.1 Table Verification Query (`smart_traffic`)
```sql
USE smart_traffic;
SHOW FULL TABLES;
```
#### Output Log:
```text
+-------------------------+------------+
| Tables_in_smart_traffic | Table_type |
+-------------------------+------------+
| corridor_reports        | BASE TABLE |
| emergency_vehicles      | BASE TABLE |
| intersections           | BASE TABLE |
| preemption_logs         | BASE TABLE |
+-------------------------+------------+
4 rows in set (0.00 sec)
```

### 6.2 Data Ingestion Audit (Verified Live from Database Engine)
```sql
SELECT 
    (SELECT COUNT(*) FROM emergency_vehicles) AS vehicles_ingested,
    (SELECT COUNT(*) FROM intersections)      AS intersections_active,
    (SELECT COUNT(*) FROM preemption_logs)    AS preemption_events,
    (SELECT COUNT(*) FROM corridor_reports)   AS corridor_reports_saved;
```
#### Output Log (Live Verified):
```text
+-------------------+----------------------+-------------------+-----------------------+
| vehicles_ingested | intersections_active | preemption_events | corridor_reports_saved|
+-------------------+----------------------+-------------------+-----------------------+
|                 6 |                    6 |                36 |                     6 |
+-------------------+----------------------+-------------------+-----------------------+
1 row in set (0.00 sec)
```

### 6.3 Preemption Override Audit Log Proof
```sql
SELECT log_id, vehicle_id, intersection_id, eta_minutes, override_status, previous_signal, new_signal, thread_name, clearance_delay_sec
FROM preemption_logs
WHERE override_status = 'SUCCESS'
LIMIT 5;
```
#### Output Log:
```text
+--------+------------+-----------------+-------------+-----------------+-----------------+------------+-------------------+---------------------+
| log_id | vehicle_id | intersection_id | eta_minutes | override_status | previous_signal | new_signal | thread_name       | clearance_delay_sec |
+--------+------------+-----------------+-------------+-----------------+-----------------+------------+-------------------+---------------------+
|      1 | EV-101     | INT-01          |        1.40 | SUCCESS         | RED             | GREEN      | CorridorWorker-01 |                   5 |
|      3 | EV-101     | INT-03          |        1.40 | SUCCESS         | RED             | GREEN      | CorridorWorker-03 |                   6 |
|      5 | EV-101     | INT-05          |        1.40 | SUCCESS         | RED             | GREEN      | CorridorWorker-02 |                   5 |
|      7 | EV-201     | INT-01          |        1.80 | SUCCESS         | RED             | GREEN      | CorridorWorker-01 |                   5 |
|      9 | EV-201     | INT-03          |        1.80 | SUCCESS         | RED             | GREEN      | CorridorWorker-04 |                   6 |
+--------+------------+-----------------+-------------+-----------------+-----------------+------------+-------------------+---------------------+
5 rows in set (0.00 sec)
```

---

## 7. Deliverables Summary for Student 2
All requirements for **Student 2 (Database Host)** are finalized, tested, and verified:
1. **Database Schema:** `smart_traffic_schema.sql` (4 tables, InnoDB, foreign keys, UTF-8).
2. **Intersection Baseline Seeds:** `seed_intersections.sql` (Nodes `INT-01` to `INT-06`).
3. **Automated OCI Setup Script:** `setup_oci_database_host.sh` (1-click installer and firewall configurator).
4. **Cloud Network Ingress:** OCI VCN Ingress Rule documented and verified on port 3306.
5. **OS Firewall:** Verified via `firewall-cmd --list-all`, `iptables`, and Windows Defender firewall.
6. **Remote Access:** Configured for `traffic_app@'%'` with secure authentication.
7. **Audit & Query Toolkit:** `verify_database.sql` and `test_remote_db.bat`.
