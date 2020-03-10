Getting started
---------------

### Install Java

SCM-Manager needs an installed Java 1.7 or newer. It is recommended to
use the [oracle jre](http://java.oracle.com/ "wikilink"). How to check
which version of Java is installed:

### Install the latest version of Java

Download java from [here](http://java.oracle.com/ "wikilink") and follow
the install instructions.

### Install SCM-Manager

Download the latest version of SCM-Manager from
[here](http://www.scm-manager.org/download/ "wikilink"), unpack the .zip
or .tar.gz package and start SCM-Manager with

### First access

\|=URL\|<http://localhost:8080>\| \|=Username\|scmadmin\|
\|=Password\|scmadmin\|

### Mercurial

Subversion and Git will work out of the box, but if you want to use
mercurial with SCM-Manager you have to install mercurial version
\*\*1.9\*\* or newer.

#### Mercurial on Windows

The installation of mercurial for SCM-Manager is very complicated on
windows, have a look at:

-   <https://bitbucket.org/sdorra/scm-manager/issue/1/no-ability-to-rename-repository>
-   <https://groups.google.com/d/msg/scmmanager/zOigMIn2RiE/xeFcruG70s8J>
-   <https://bitbucket.org/sdorra/build-win-hg-packages>

SCM-Manager comes with the option to install packages for windows to
simplify this setup. To use such a package just login as Administrator,
goto \"Repository Types\", click the \"Start Configuration Wizard\" and
Choose \"Download and install\".

If you see an error like the following:

Then you have to install [Microsoft Visual C++ 2008 SP1 Redistributable
Package
(x86)](http://www.microsoft.com/en-us/download/details.aspx?id=5582 "wikilink").
Note you have to use the x86 package and not the x64 package, because we
use 32bit python in SCM-Manager on Windows. For more informations have a
look at
[\#522](https://bitbucket.org/sdorra/scm-manager/issue/552/hg-repo-creation-failed "wikilink").
