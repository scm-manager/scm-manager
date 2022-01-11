---
title: Debian/Ubuntu
subtitle: Installation for debian based linux distributions
displayToc: true
---

## Quickstart

The following code block will configure an apt repository for scm-manager and install it.

```bash
echo 'deb [arch=all] https://packages.scm-manager.org/repository/apt-v2-releases/ stable main' | sudo tee /etc/apt/sources.list.d/scm-manager.list
sudo apt-key adv --recv-keys --keyserver hkps://keys.openpgp.org 0x975922F193B07D6E
sudo apt-get update
sudo apt-get install scm-server
```

After the installation of the package scm-manager will automatically start on port 8080.
There you have to create your initial admin account with an initialization token which you can find inside your server logs.
You can find more detailed information here: [first startup](../../first-startup/)

## Detailed installation

To install SCM-Manager as a debian package (.deb), we have to configure an apt repository.
Create a file at `/etc/apt/sources.list.d/scm-manager.list` with the following content:

```text
deb [arch=all] https://packages.scm-manager.org/repository/apt-v2-releases/ stable main
```

This will add the apt repository of the scm-manager stable releases to the list of your apt repositories.
To ensure the integrity of the debian packages we have to import the gpg key for the repository.

```bash
sudo apt-key adv --recv-keys --keyserver hkps://keys.openpgp.org 0x975922F193B07D6E
```

After we have imported the gpg key, we can update the package index and install scm-manager:

```bash
sudo apt-get update
sudo apt-get install scm-server
```

The package will configure a systemd service called scm-server.
The service is enabled and started with installation of the package.
After the service is started, scm-manager should be reachable on port 8080
The default username is `scmadmin` with the password `scmadmin`.

## Troubleshooting

If the service does not start have a look at the systemd journal:

```bash
journalctl -u scm-server
```

For further information have a look at the scm-manager logs at `/var/log/scm`.

## Home directory

SCM-Manager stores all its information in its home directory at `/var/lib/scm`.

## Configuration

Most of the configuration of scm-manager can be configured via the web interface.
But the startup and the web server configuration must be configured via configuration files.
The default configuration of the debian package should match 90% of the use cases,
if you have to change something ensure you know what you are doing.

To configure the startup have a look at `/etc/default/scm-server`.
To configure logging and the webserver, `/etc/scm` is the right place.
