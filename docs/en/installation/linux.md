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

Download [scm-server-2.0.0-rc5-app.tar.gz](https://maven.scm-manager.org/nexus/service/local/repositories/releases/content/sonia/scm/scm-server/2.0.0-rc5/scm-server-2.0.0-rc5-app.tar.gz)
and verify the checksum (sha1: 3b2dff3fda0c46362c518be37edd4e77bccc88bb).

```bash
wget https://maven.scm-manager.org/nexus/service/local/repositories/releases/content/sonia/scm/scm-server/2.0.0-rc5/scm-server-2.0.0-rc5-app.tar.gz
echo "3b2dff3fda0c46362c518be37edd4e77bccc88bb *scm-server-2.0.0-rc5-app.tar.gz" | sha1sum -c -
```

Extract the archive:

```bash
tar xvfz scm-server-2.0.0-rc5-app.tar.gz -C /opt
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
