---
title: Unix
subtitle: General unix installation
displayToc: true
---

## Requirements

Ensure that Java JRE is installed at least in version 11.
If you want to use [Mercurial](https://www.mercurial-scm.org/), ensure it is installed on your machine.

## Installation

Grab the latest version and checksum from [download page](/download) and replace `<version>` and `<checksum>` in the code blocks below.
Download and verify the checksum.

```bash
wget https://packages.scm-manager.org/repository/public/sonia/scm/scm-packaging/unix/<version>/unix-<version>-app.tar.gz
wget https://packages.scm-manager.org/repository/releases/content/sonia/scm/scm-server/2.0.0-rc8/scm-server-2.0.0-rc8-app.tar.gz
echo "<checksum> *unix-<version>-app.tar.gz" | sha1sum -c -
```

Extract the archive:

```bash
tar xvfz unix-<version>-app.tar.gz -C /opt
```

The application can be started by using the scm-server script.

```bash
/opt/scm-server/bin/scm-server
```

if you want to start the application in background, we can use the `start` parameter.

```bash
/opt/scm-server/bin/scm-server start
```

After the scm-manager is started, it should be reachable on port 8080
There you have to create your initial admin account with an initialization token which you can find inside your server logs.
You can find more detailed information here: [first startup](../../first-startup/)

## Troubleshooting

If SCM-Manager does not start have a look at the logs `/opt/scm-server/logs` or `~/.scm/logs`

## Home directory

SCM-Manager stores all its information in its home directory.
The directory is located in the home directory of the user, which has started the process, and is named `.scm`.

## Configuration

Most of the configuration of scm-manager can be configured via the web interface.
But the startup and the web server configuration must be configured via configuration files.
The default configuration of the debian package should match 90% of the use cases,
if you have to change something ensure you know what you are doing.

To configure the startup have a look at `/opt/scm-server/bin/scm-server`.
To configure logging and the webserver, `/opt/scm-server/conf` is the right place.
