# How to Check If MySQL Is Installed on WSL2

**Category:** DevOps > Environment Setup > WSL2  
**Tags:** `mysql`, `wsl2`, `linux`, `database`, `installation`  
**Last Updated:** 2026-06-24

---

## Overview

This guide covers multiple ways to verify whether MySQL is installed inside a WSL2 (Windows Subsystem for Linux 2) environment.

---

## Methods to Check MySQL Installation

### Method 1: Check MySQL Version

```bash
mysql --version
```

- ✅ **Installed:** Returns something like `mysql  Ver 8.0.36 Distrib 8.0.36, for Linux (x86_64)`
- ❌ **Not installed:** Returns `command not found`

---

### Method 2: Check MySQL Service Status

```bash
systemctl status mysql
```

Or using the legacy service command:

```bash
service mysql status
```

---

### Method 3: Locate the MySQL Binary

```bash
which mysql
```

- ✅ **Installed:** Returns path like `/usr/bin/mysql`
- ❌ **Not installed:** Returns nothing (empty output)

---

### Method 4: List Installed Packages (Debian/Ubuntu)

```bash
dpkg -l | grep mysql
```

Lists all MySQL-related packages currently installed on the system.

---

### Method 5: Check via APT

```bash
apt list --installed 2>/dev/null | grep mysql
```

---

## Quick Reference

| Command | Installed Output | Not Installed Output |
|---|---|---|
| `mysql --version` | Version string | `command not found` |
| `which mysql` | `/usr/bin/mysql` | *(empty)* |
| `dpkg -l \| grep mysql` | Package list | *(empty)* |
| `service mysql status` | Service status | `mysql: unrecognized service` |

---

## If MySQL Is Not Installed

Install MySQL on Ubuntu/Debian-based WSL2:

```bash
sudo apt update
sudo apt install mysql-server -y
```

Start the MySQL service:

```bash
sudo service mysql start
```

Verify it's running:

```bash
sudo service mysql status
```

---

## Related Articles

- How to configure MySQL after installation on WSL2
- Connecting to WSL2 MySQL from Windows host
- Common MySQL errors on WSL2 and how to fix them
