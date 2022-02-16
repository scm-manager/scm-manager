---
title: Redhat/CentOS/Fedora
subtitle: Installation for Red Hat based linux distributions
displayToc: true
---

## Quickstart

The following code block will configure a yum repository for scm-manager and install it.

```bash
cat << EOF | sudo tee /etc/yum.repos.d/SCM-Manager.repo
[scm-manager]
name=SCM-Manager Repository
baseurl=https://packages.scm-manager.org/repository/yum-v2-releases/
enabled=1
gpgcheck=1
priority=1
gpgkey=file:///etc/pki/rpm-gpg/SCM-Manager
EOF
sudo curl -o /etc/pki/rpm-gpg/SCM-Manager https://packages.scm-manager.org/repository/keys/gpg/oss-cloudogu-com.pub
sudo yum install scm-server
```

After the installation of the package scm-manager will automatically start on port 8080.
There you have to create your initial admin account with an initialization token which you can find inside your server logs.
You can find more detailed information here: [first startup](../../first-startup/)

## Detailed installation

To install SCM-Manager as a redhat package (.rpm), we have to configure a yum repository.
Create a file at `/etc/yum.repos.d/SCM-Manager.repo` with the following content:

```ini
[scm-manager]
name=SCM-Manager Repository
baseurl=https://packages.scm-manager.org/repository/yum-v2-releases/
enabled=1
gpgcheck=1
priority=1
gpgkey=file:///etc/pki/rpm-gpg/SCM-Manager
```

This will add the yum repository of the scm-manager stable releases to the list of your yum repositories.
To ensure the integrity of the rpm packages we have to import the gpg key for the repository.

```bash
sudo curl -o /etc/pki/rpm-gpg/SCM-Manager https://packages.scm-manager.org/repository/keys/gpg/oss-cloudogu-com.pub
```

After we have imported the gpg key, we can install scm-manager:

```bash
sudo yum install scm-server
```

The package will configure a systemd service called scm-server.
The service is enabled and started with installation of the package.
After the service is started, scm-manager should be reachable on port 8080
The default username is `scmadmin` with the password `scmadmin`.

## Troubleshooting

### Upgrade from SCM-Manager 1.x

If you had an SCM-Manager 1.x installed before, please remove (or better back up) the old `/opt/scm-server` directory. This must not exist before installing the new 2.x version.

### Service does not start

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
The default configuration of the redhat package should match 90% of the use cases,
if you have to change something ensure you know what you are doing.

To configure the startup have a look at `/etc/default/scm-server`.
To configure logging and the webserver, `/etc/scm` is the right place.
