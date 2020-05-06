---
title: SCM-Jenkins-Plugin
---

The jenkins plugin will ping your Jenkins CI server when a new commit is
pushed to SCM-Manager. In SCM-Manager exists two differnt methods to
trigger jenkins builds.

### Per repository configuration

The \"per repository configuration\" method requires a configuration for
each repository. The following parameters must be configured:

-   The url of the jenkins server inclusive the context path e.g.:
    <http://ci.scm-manager.org:8080/jenkins>
-   The name of the jenkins job
-   Jenkins trigger authentication token
-   Username of the jenkins user. This option is only required if your
    jenkins job does not allow anonymous access
-   API Token of the jenkins user. This token is used for
    authentication. You could get your API Token from your jenkins
    server at
    <http://yourjenkinsserver/contextpath/user/username/configure>

Note for this setup the jenkins job have to be configured for remote
trigger (Trigger builds remotely).

### Global configuration

The global configuration has only support for git and mercurial. If you
want to use the jenkins plugin with subversion repositories, you have to
use the \"per repository configuration\". The global configuration
method uses push notifications from the git-plugin and mercurial-plugin
for jenkins. SCM-Manager will send the url of the changed repository
after each successful push, jenkins will build each repository which
this scm url and enabled polling.

To use the \"global configuration\" method, you have to note a few
things:

-   You have to insert the url to your jenkins server
    (Config-\>General-\>Jenkins Configuration-\>Url) the url must
    conatin the context path of jenkins e.g.:
    <http://ci.scm-manager.org:8080/jenkins>.
-   Be sure the base url contains the full qualified hostname to your
    scm-manager server (Config-\>General-\>General Settings-\>Base Url).
-   All jenkins repositories have to be configured for polling (the
    interval does not matter, a good value would be once a day).
-   If you are using \"Matrix based security\" on jenkins, be sure you
    use at least version 1.43 of the mercurial-plugin for jenkins.

#### Links

-   <http://kohsuke.org/2011/12/01/polling-must-die-triggering-jenkins-builds-from-a-git-hook/>
-   <https://wiki.jenkins-ci.org/display/JENKINS/Subversion+Plugin>
-   <https://wiki.jenkins-ci.org/display/JENKINS/Mercurial+Plugin>
-   <https://wiki.jenkins-ci.org/display/JENKINS/Git+Plugin>
-   <https://github.com/jenkinsci/mercurial-plugin/pull/32>
