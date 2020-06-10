---
title: Redmine Plugin
---

Is based on [scm-jira-plugin](../jira-plugin/).

The plugin enables the following features to integrate SCM-Manager to
[Redmine](http://www.redmine.org/):

- Turn issue keys in changeset descriptions to links for redmine
- Updates a redmine issue if the issue key is found in a changeset description
- Close a Redmine issue if the issue key and a auto close (close, fix, resolve, \...) 
    word is found in the changeset description

### Installation and configuration

1.  Enable [Redmine rest authentication](https://www.redmine.org/projects/redmine/wiki/Rest_api#Authentication), basically
    \'you have to check Enable REST API in Administration -\> Settings
    -\> Authentication\'
2.  Install redmine-plugin over the plugin center in scm-manager
3.  Configure the plugin, select a repository to enable the
    redmine-plugin for this repository
4.  To link issues commit must be match the following: \'(\#issue\_id)
    your commit message\'
5.  **Note**: For the auto close and update feature it is necessary
    that users have the same names and passwords in SCM-Manager and
    Redmine
