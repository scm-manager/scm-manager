---
title: Linux
subtitle: General linux installation
displayToc: true
---
## Requirements

Ensure that Java JRE is installed at least in version 8.
Recommended is Java 11.

If you want to use [Mercurial](https://www.mercurial-scm.org/), ensure it is installed on your machine.

## Installation

Download [scm-server-2.0.0-rc8-app.tar.gz](https://packages.scm-manager.org/repository/releases/content/sonia/scm/scm-server/2.0.0-rc8/scm-server-2.0.0-rc8-app.tar.gz)
and verify the checksum (sha1: 8bf465525d5a8c5907d1f74096af1783bc0b2fa7).

```bash
wget https://packages.scm-manager.org/repository/releases/content/sonia/scm/scm-server/2.0.0-rc8/scm-server-2.0.0-rc8-app.tar.gz
echo "8bf465525d5a8c5907d1f74096af1783bc0b2fa7 *scm-server-2.0.0-rc8-app.tar.gz" | sha1sum -c -
```

Extract the archive:

```bash
tar xvfz scm-server-2.0.0-rc8-app.tar.gz -C /opt
```

## Start

The application can be started by using the scm-server script.

```bash
/opt/scm-server/bin/scm-server
```

## Daemonize

To start the application in background, we can use the `start` parameter.

```bash
/opt/scm-server/bin/scm-server start
```
