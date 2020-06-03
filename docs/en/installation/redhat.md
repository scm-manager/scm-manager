---
title: Redhat/CentOS/Fedora
subtitle: Installation for Red Hat based linux distributions
displayToc: true
---

## Directory structure

Since version 1.39, SCM-Manager is delivered as RPM and DEB package. The package are generated with the [nativepkg-maven-plugin](https://github.com/sdorra/nativepkg-maven-plugin). The following table shows the directory structure which will be created by the packages:

File | Description
--- | ---
/opt/scm-server | Main directory for scm-server installation
/etc/default/scm-server | Default settings for scm-server
/etc/init.d/scm-server | Start script for scm-server
/var/cache/scm/work | Cache directory
/var/log/scm | Logging directory
/var/lib/scm | SCM-Manager home directory

The package will create a user and group which are called scm. The scm user will be the owner of the process. The init script uses jsvc to start the scm-server process, which makes it possible to run scm-server on port 80 without running it as root user. The main settings for the server process are stored at /etc/default/scm-server.

## Installation

The RPM package is tested with Fedora and Centos. Create a new files at /etc/yum.repos.d/SCM-Manager.repo with the following content to install the scm-manager repository:

```text
[scm-releases]
name=SCM-Manager Releases
baseurl=https://packages.scm-manager.org/repository/yum-v1-releases/
enabled=1
gpgcheck=0
```

After file creation execute the following command to install scm-server:


```bash
# install the scm-server package
sudo yum install scm-server
```

## Migration from ApplicationServer or Standalone version

To migrate from an existing installation, you have to the following steps:

* Stop the old service
* Move the folder /opt/scm-server, if it exists
* Install the package
* Copy the content of your existing scm home directory to /var/lib/scm
* Change the owner of the directory and all containing files to scm:scm (chown -R scm:scm /var/lib/scm)
* Verify *repositoryDirectory* in /var/lib/scm/config/[git|hg|svn].xml], make sure they point to respective /var/lib/scm/repositories/[git|hg|svn] and not your old location. Otherwise the repository health check will fail.
* Optional: Reapply changes to /opt/scm-server/conf/server-config.xml and /opt/scm-server/conf/logging.xml
* Start scm-server (/etc/init.d/scm-server start)
