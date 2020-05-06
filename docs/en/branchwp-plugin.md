---
title: Branch Write Protect Plugin
---

### Installation

-   Login in as administrator
-   Open Plugins
-   Install scm-branchwp-plugin
-   Restart your applicationserver

### Usage

After the restart you should see a \"Branch write protect\" tab for each
repository. On this tab you are able to set branch write protections for
users and groups. Here are some rules for the usage of the branchwp
plugin:

-   Administrators and repository owner have always write access.
-   Grant write permissions on the \"Permission\" tab for every user or
    group who should write to any branch in the repository.
-   If the branchwp plugin is enabled, nobody can write to the
    repository expect administrators, repository owners and the
    specified rules.

### Notes

The branchwp plugin works only for Git and Mercurial, for Subversion
have a look at the
[pathwp-plugin](http://plugins.scm-manager.org/scm-plugin-backend/page/detail/sonia.scm.plugins/scm-pathwp-plugin.html).

Since version 1.2 of the plugin it is possible to define deny
permissions and placeholders for branch names. Deny permissions are
handled always before allow permissions. At the state of version 1.2 the
following placeholders are available:

-   {username} - will be replaced with the username of the current user
-   {mail} - will be replaced with the e-mail address of the current
    user

### Known issues
- [#235](https://github.com/scm-manager/scm-manager/issues/235 "branchwp plugin can not use on git")
