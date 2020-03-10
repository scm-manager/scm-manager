# Incompatible subversion db format #

SCM-Manager versions prior to 1.36 are creating incompatible subversion repositories, if the subversion option "with 1.7 Compatible" is enabled. This subversion repositories are neither compatible with svn 1.7 nor svn 1.8. These repositories are marked as unhealthy by SCM-Manager version 1.36 and above.

This incompatible repositories must be converted, before they can be used with SCM-Manager 1.36 and above. Note the convert process can take some time and need some space, because it creates a backup for each converted repository. Follow the steps below to convert all incompatible subversion repositories of one SCM-Manager instance.

* stop SCM-Manager
* create a full backup of your scm home directory
* download the convert util from [here](https://maven.scm-manager.org/nexus/content/repositories/releases/sonia/scm/scm-fixsvndb5-cli/1.0.1/scm-fixsvndb5-cli-1.0.1-jar-with-dependencies.jar)
* execute the convert util with your scm home directory as parameter e.g.:

```
#!bash

java -jar scm-fixsvndb5-cli-1.0.1-jar-with-dependencies.jar /path/to/.scm
```

* start SCM-Manager

For more informations have a look at:

* [Issue #519](https://bitbucket.org/sdorra/scm-manager/issue/519/default-svn-repository-format-setting)
* [Subversion release notes](https://subversion.apache.org/docs/release-notes/1.7.html#revprop-packing)