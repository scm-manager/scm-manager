---
title: Logging
subtitle: Configuration and locations of SCM-Manager logging
---

SCM-Manager logs information which can be useful, if the system does not behave as expected.
The logging behavior depends on your operating system and installation.

| Type of Installation | Logging |
|----------------------|---------|
| Docker | stdout |
| RPM | /var/log/scm |
| DEB | /var/log/scm |
| Unix | $BASEDIR/logs |
| Mac OS X | ~/Library/Logs/SCM-Manager |
| Windows | $BASEDIR\logs |

The location of the **$BASEDIR** can be found [here](../basedirectory/).

## Configuration

The logging behaviour of SCM-Manager can be configured via an xml file.
The syntax and properties can be found [here](http://logback.qos.ch/manual/configuration.html).
The location of the file depends also on the type of installation.

| Type of Installation | Path |
|----------------------|---------|
| Docker | /opt/scm-server/conf/logging.xml |
| RPM | /etc/scm/logging.xml |
| DEB | /etc/scm/logging.xml |
| Unix | $EXTRACT_PATH/scm-server/conf/logging.xml |
| Mac OS X | $EXTRACT_PATH/scm-server/conf/logging.xml |
| Windows | $EXTRACT_PATH/scm-server/conf/logging.xml |

**$EXTRACT_PATH** is the path were you etract the content of the package.
