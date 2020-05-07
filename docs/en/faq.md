---
title: Frequently Asked Questions
---

### What are the username and the password in the default installation?

Username: `scmadmin`\
Password: `scmadmin`

### Where does SCM-Manager store its configuration, log files and the repositories?

SCM-Manager stores the complete data in a directory called .scm (the
SCM-Manager home directory). This directory is located in the home
directory of the user which is the owner of the process. Except for [rpm
and deb](RPM%20and%20DEB%20packages.md)
installations, for those installations the home directory is located at
/var/lib/scm.

### How can I change the SCM-Manager home directory?

You could change the SCM-Manager home directory in a few ways:

-   Edit the scm.properties (WEB-INF/classes) file and add the path to
    your folder f.e. `scm.home=/var/scm`
-   Set an environment variable SCM\_HOME with the path of your
    directory
-   Start your application server with a java property called `scm.home`
    f.e. `-Dscm.home=/var/scm`

### Can I create a directory structure for scm-manager repositories?

Yes, since version 1.9 you can create directory structures. You can just
use a \"/\" in the name of the repository to create the structure. For
example the repositories Project/module-1, Project/module-2 and
OtherProject/module-1 will result in the following structure.

```text
+ Project
| - module-1
| - module-2
+ OtherProject
| - module-1
```

For more information have a look at [#47](https://github.com/scm-manager/scm-manager/issues/47 "Support for directory structure").

### After creation of a new public repository I am trying to clone it anonymously, but I got request of user and password. What am I doing wrong?

You have to enable \"Allow Anonymous Access\" at Config-\>General.

### Where does SCM-Manager stores it log files?

SCM-Manager stores the log files in a directory called \"logs\" which is
located in the home directory (see question \"Where does SCM-Manager
store its configuration, log files and the repositories?\").

### How do I enable trace logging?

Edit scm-server/conf/logging.xml change the line from:

```xml
<logger name="sonia.scm" level="INFO" />
```
to:

```xml
<logger name="sonia.scm" level="TRACE" />
```

If you are using the war version with an application server such as
tomcat, you have to edit the logback.xml in WEB-INF/classes.

### How do I install plugins?

Select Config-\>Plugins. This is supposed to show you a list of all
available plugins to install. It is not a place to configure existing
plugins. Install Package does not take you to the install screen\... If
you only see the installed plugins, see the next question.

### Why don\'t I see any installable plugins on the plugin tab?

Is the SCM-Manager server behind a proxy server? Then you have to
configure your proxyserver at Config-\>General.
