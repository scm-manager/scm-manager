---
title: Frequently Asked Questions
---

### What are the username and the password in the default installation?

Username: `scmadmin`\
Password: `scmadmin`

### Where does SCM-Manager store its configuration, data and repositories?

All data which is created by SCM-Manager, is stored in the SCM-Manager  base directory.
The location of the base directory depends on your type of installation.
Please have a look at the [documentation](../administration/basedirectory/).

### How can I change the SCM-Manager home directory?

There are several ways to change the location of the home directory: [documentation](../administration/basedirectory/#change-base-directory-location)

### Where does SCM-Manager stores it log files?

The location of the log files depends on your operation system and the type of installation.
Please have a look at the [documentation](../administration/logging/).

### How do I enable trace logging?

Find the location of your `logging.xml` in the [documentation](../administration/logging/#configuration) and change the following line from:

```xml
<logger name="sonia.scm" level="INFO" />
```
to:

```xml
<logger name="sonia.scm" level="TRACE" />
```

After changing the configuration, SCM-Manager must be restarted.

### How do I install plugins?

Find the plugin you like to install at [plugins](/plugins#categories) and follow the installation instructions on the install page of the plugin.

### How can I import my existing (git|mercurial|subversion) repository

Please have a look on [this](../import/) detailed instructions.
