---
title: Unix
subtitle: General unix installation
displayToc: true
---

# Getting started

### Install Java

SCM-Manager needs an installed Java 1.7 or newer. It is recommended to use the [oracle jre](http://java.oracle.com/). 
How to check which version of Java is installed:

```bash
java -version
```

Download java from [here](http://java.oracle.com/) and follow the install instructions.

### Install SCM-Manager

Download the latest version of SCM-Manager from
[here](http://www.scm-manager.org/download/), unpack the .zip
or .tar.gz package and start SCM-Manager with

```bash
scm-server/bin/scm-server
```

### First access

|              |                         |
| ------------ | ----------------------- |
| **URL**      | <http://localhost:8080> |
| **Username** | scmadmin                |
| **Password** | scmadmin                |

### Mercurial

Subversion and Git will work out of the box, but if you want to use
mercurial with SCM-Manager you have to install mercurial version
**1.9** or newer.
