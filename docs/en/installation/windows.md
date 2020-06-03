---
title: Windows
subtitle: Install scm-manager on windows
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

The installation of mercurial for SCM-Manager is very complicated on
windows, have a look at:

- [#1](https://bitbucket.org/sdorra/scm-manager/issues/1/no-ability-to-rename-repository)
- [xeFcruG70s8J](https://groups.google.com/d/msg/scmmanager/zOigMIn2RiE/xeFcruG70s8J "Python/Hg Package Build Process")
- [build-win-hg-packages](https://bitbucket.org/sdorra/build-win-hg-packages)

SCM-Manager comes with the option to install packages for windows to
simplify this setup. To use such a package just login as Administrator,
goto \"Repository Types\", click the \"Start Configuration Wizard\" and
Choose \"Download and install\".

If you see an error like the following:

```text
sonia.scm.repository.RepositoryException: command exit with error 14001 and message: 'The application has failed to start because its side-by-side configuration is incorrect. Please see the application event log or use the command-line sxstrace.exe tool for more detail.'
```

Then you have to install [Microsoft Visual C++ 2008 SP1 Redistributable Package 
(x86)](http://www.microsoft.com/en-us/download/details.aspx?id=5582).
Note you have to use the x86 package and not the x64 package, because we
use 32bit python in SCM-Manager on Windows. For more informations have a
look at
[#522](https://bitbucket.org/sdorra/scm-manager/issue/552/hg-repo-creation-failed).
