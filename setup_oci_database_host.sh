#!/usr/bin/env bash
# =============================================================================
# SMART CITY TRAFFIC SIGNAL & EMERGENCY VEHICLE GREEN CORRIDOR ROUTER
# Student 2: Database Host Automated Deployment Script for OCI VM 2
# OS Support: Oracle Linux 8 / 9, RHEL 8 / 9, Ubuntu 22.04 / 24.04 LTS
# =============================================================================

set -e

echo "======================================================================"
echo " Starting Student 2 Database Host Automated Setup on OCI VM 2"
echo "======================================================================"

# Determine Package Manager
if command -v dnf &> /dev/null; then
    PKG_MGR="dnf"
elif command -v yum &> /dev/null; then
    PKG_MGR="yum"
elif command -v apt-get &> /dev/null; then
    PKG_MGR="apt"
else
    echo "[!] Unsupported package manager. Please install MySQL manually."
    exit 1
fi

echo "[1/6] Installing MySQL Server ($PKG_MGR)..."
if [ "$PKG_MGR" = "dnf" ] || [ "$PKG_MGR" = "yum" ]; then
    sudo $PKG_MGR install -y mysql-server
    sudo systemctl enable --now mysqld
    MYSQL_CONF="/etc/my.cnf.d/mysql-server.cnf"
    if [ ! -f "$MYSQL_CONF" ]; then
        MYSQL_CONF="/etc/my.cnf"
    fi
elif [ "$PKG_MGR" = "apt" ]; then
    sudo apt-get update -y
    sudo apt-get install -y mysql-server
    sudo systemctl enable --now mysql
    MYSQL_CONF="/etc/mysql/mysql.conf.d/mysqld.cnf"
fi

echo "[2/6] Configuring MySQL Network Binding (bind-address = 0.0.0.0)..."
if [ -f "$MYSQL_CONF" ]; then
    if grep -q "bind-address" "$MYSQL_CONF"; then
        sudo sed -i 's/bind-address.*/bind-address = 0.0.0.0/' "$MYSQL_CONF"
    else
        echo -e "\n[mysqld]\nbind-address = 0.0.0.0" | sudo tee -a "$MYSQL_CONF" > /dev/null
    fi
fi

# Restart MySQL service
if systemctl is-active --quiet mysqld; then
    sudo systemctl restart mysqld
elif systemctl is-active --quiet mysql; then
    sudo systemctl restart mysql
fi

echo "[3/6] Configuring OS-Level Firewall for Port 3306 (TCP)..."
if command -v firewall-cmd &> /dev/null && systemctl is-active --quiet firewalld; then
    echo "Configuring firewalld..."
    sudo firewall-cmd --permanent --zone=public --add-port=3306/tcp
    sudo firewall-cmd --reload
    sudo firewall-cmd --list-ports
elif command -v ufw &> /dev/null && sudo ufw status | grep -q "Status: active"; then
    echo "Configuring ufw..."
    sudo ufw allow 3306/tcp
    sudo ufw status
else
    echo "Configuring iptables..."
    sudo iptables -I INPUT -p tcp --dport 3306 -m state --state NEW,ESTABLISHED -j ACCEPT
    if command -v netfilter-persistent &> /dev/null; then
        sudo netfilter-persistent save
    fi
fi

echo "[4/6] Initializing Database Schema (smart_traffic_schema.sql)..."
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if [ -f "$SCRIPT_DIR/smart_traffic_schema.sql" ]; then
    sudo mysql < "$SCRIPT_DIR/smart_traffic_schema.sql"
    echo "[+] smart_traffic_schema.sql executed successfully."
else
    echo "[-] Warning: smart_traffic_schema.sql not found in $SCRIPT_DIR"
fi

if [ -f "$SCRIPT_DIR/seed_intersections.sql" ]; then
    sudo mysql < "$SCRIPT_DIR/seed_intersections.sql"
    echo "[+] seed_intersections.sql executed successfully."
fi

echo "[5/6] Creating Remote User 'traffic_app'@'%'..."
sudo mysql <<EOF
CREATE USER IF NOT EXISTS 'traffic_app'@'%' IDENTIFIED BY 'SmartCity2026!';
ALTER USER 'traffic_app'@'%' IDENTIFIED BY 'SmartCity2026!';
GRANT ALL PRIVILEGES ON smart_traffic.* TO 'traffic_app'@'%';
FLUSH PRIVILEGES;
SELECT user, host FROM mysql.user WHERE user='traffic_app';
EOF

echo "[6/6] Verifying Network Listening State..."
if command -v ss &> /dev/null; then
    sudo ss -tlpn | grep 3306 || true
elif command -v netstat &> /dev/null; then
    sudo netstat -tlpn | grep 3306 || true
fi

PUBLIC_IP=$(curl -s -m 3 https://ifconfig.me || curl -s -m 3 https://api.ipify.org || echo "<YOUR_VM2_PUBLIC_IP>")
PRIVATE_IP=$(hostname -I | awk '{print $1}')

echo "======================================================================"
echo " [SUCCESS] OCI VM 2 Database Host Setup Completed!"
echo "======================================================================"
echo " - Database Name : smart_traffic"
echo " - Port          : 3306"
echo " - Remote User   : traffic_app"
echo " - Remote Pass   : SmartCity2026!"
echo " - VM2 Private IP: $PRIVATE_IP"
echo " - VM2 Public IP : $PUBLIC_IP"
echo ""
echo " >>> OCI VCN SECURITY LIST INGRESS RULE REMINDER <<<"
echo " Ensure your OCI VCN Security List has an Ingress Rule:"
echo "   Source CIDR: 0.0.0.0/0 (or Student 1's IP)"
echo "   Protocol   : TCP"
echo "   Dest Port  : 3306"
echo ""
echo " Share the following connection string with Student 1 (Application Host):"
echo " DB_URL=\"jdbc:mysql://$PUBLIC_IP:3306/smart_traffic?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC\""
echo "======================================================================"
