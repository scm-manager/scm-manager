---
title: Release Notes 1.60 - 1.40
displayToc: true
---

**note**

-   Versions prior to 1.36 are creating incompatible subversion
    repositories, if the subversion option \"with 1.7 Compatible\" is
    enabled. [read more](../healthchecks/svn-incompatible-dbformat/)
-   since version 1.18 scm-manager requires mercurial 1.9 or newer
-   since version 1.49 Java 7 or newer is required
-   version 1.58 and 1.59 are not working on java 7, but version 1.60
    restored java 7 support
-   java 9 and 10 are supported since 1.60

SCM-Manager 1.60
----------------

**fixed bugs**

-   restored java 7 compatibility (broken since 1.58), see \<\<issue
    972\>\> and \<\<issue 982\>\>
-   fixed build on java 9
-   fixed execution on java 9 and 10



**improvements**

-   encrypt cli configuration with aes instead of pbe, see \<\<issue
    979\>\> and \<\<issue 978\>\>



**library updates**

-   update commons-daemon to version 1.1.0

SCM-Manager 1.59
----------------

**fixed bugs**

-   mercurial: fix hgweb execution for mercurial versions prior 4.1, see
    [\#976](https://bitbucket.org/sdorra/scm-manager/issues/976/issue-with-158-and-mercurial)
-   mercurial: make {extras} work on old versions of Hg, see [PR
    \#41](https://bitbucket.org/sdorra/scm-manager/pull-requests/41/make-extras-work-on-old-versions-of-hg/diff)
    and
    [\#971](https://bitbucket.org/sdorra/scm-manager/issues/971/commit-listening-requires-at-least)

SCM-Manager 1.58
----------------

**improvements**

-   mercurial: support for httppostargs protocol, see \<\<issue 970\>\>
-   mercurial: prevent
    [CVE-2018-1000132](https://cve.mitre.org/cgi-bin/cvename.cgi?name=2018-1000132),
    see \<\<issue 970\>\>
-   mercurial: dded option to disable ssl validation for scm hooks, see
    \<\<issue 959\>\>
-   removed never released scm-dao-orientdb module



**library updates**

-   update javahg to 0.13
-   update commons-beanutils to 1.9.3
-   update commons-collections to 3.2.2
-   update httpclient to 4.5.5
-   update slf4j to 1.7.25
-   update logback to 1.2.3
-   update jackson to 1.9.13
-   update apache shiro to version 1.3.2
-   update from sonatype aether to eclipse aether version 1.1.0

SCM-Manager 1.57
----------------

**improvements**

-   treat update of a git tag as delete and create for hooks



**fixed bugs**

-   fixed handling of resources with spaces in its id, see \<\<issue
    965\>\>



**library updates**

-   update svnkit to version 1.9.0-scm3

SCM-Manager 1.56
----------------

**fixed bugs**

-   fixed high cpu load after subversion client connection abort, see
    \<\<issue 939\>\>
-   fix integer overflow of request with body larger than 4gb, see
    \<\<issue 953\>\>

SCM-Manager 1.55
----------------

**improvements**

-   added option to disallow non fast-forward git pushes



**fixed bugs**

-   fixes usage of named cache configurations, see \<\<issue 943\>\>
-   fixed update of git repositories with empty git default branch, see
    issue \<\<issue 903\>\>
-   remove work directory after package upgrade, see \<\<issue 923\>\>
-   prevent binary data in mercurial {extras} from interfering with
    UTF-8 decoding, see
    [\#PR-39](https://bitbucket.org/sdorra/scm-manager/pull-requests/39)



**library updates**

-   update jgit to version v4.5.3.201708160445-r-scm1
-   update svnkit to version 1.9.0-scm1

SCM-Manager 1.54
----------------

**improvements**

-   added experimetal support for git-lfs,
    [\#PR-27](https://bitbucket.org/sdorra/scm-manager/pull-requests/27)
-   improve git client detection to include jgit
-   git repositories are now accessible with the \".git\" suffix



**fixed bugs**

-   fix repository browsing with mercurial 4.x
-   fixing test execution on german / windows machines



**library updates**

-   update jgit to v4.5.2.201704071617-r-scm1
-   update javahg to 0.8-scm1
-   update jetty to version 1.19.4
-   update jetty to version 7.6.21.v20160908

SCM-Manager 1.53
----------------

**fixed bugs**

-   fix jax-rs classpath conflict, see \<\<issue 916\>\>



**library updates**

-   update nativepkg-maven-plugin to version 1.1.4

SCM-Manager 1.52
----------------

**improvements**

-   added support for gtld email domains, see \<\<issue 909\>\>
-   improved performance by creating an adapter between scm and shiro
    caches, see \<\<issue 781\>\>
-   improved rest api documentation, see
    <https://docs.scm-manager.org/restdocs/1.52/>



**library updates**

-   update svnkit to version 1.8.15-scm1
-   update enunciate to version 2.9.1

SCM-Manager 1.51
----------------

**improvements**

-   update svnkit to version 1.8.14-scm1 in order to support subversion
    1.9 new fsfs repository format, see \<\<issue 858\>\>



**fixed bugs**

-   fix wrong subversion urls behind a reverse proxy, see \<\<issue
    889\>\>
-   svn: fix wrong error message during commit on a locked file, see
    \<\<issue 897\>\>
-   fix wrong key usage during encoding in DefaultCipherHandler, see
    \<\<issue 887\>\>



**library updates**

-   update jersey to version 1.19.3
-   update slf4j to version 1.7.22
-   update logback to version 1.1.10
-   updated jgit v4.5.0.201609210915-r-scm1
-   fix wrong subversion urls behind a reverse proxy, see \<\<issue
    889\>\>

SCM-Manager 1.50
----------------

**improvements**

-   added reusable components for branch and tag combo boxes
-   option to define default branch for git repositories \<\<issue
    873\>\>
-   added primary principal as request attribute to allow subject
    logging for access logs, see \<\<issue 877\>\>
-   treat HEAD, OPTIONS and TRACE as mercurial read requests not only
    GET, see issue \<\<issue 859\>\>
-   added new hook context api for tags



**fixed bugs**

-   send http status code 401 unauthorized on failed git authentication,
    see issue \<\<issue 870\>\>
-   fix npe when GitHookBranchProvider tries to collect a tag as branch,
    see issue \<\<issue 865\>\>

SCM-Manager 1.49
----------------

**improvements**

-   reduce event bus logging
-   added RepositoryHookITCase to test repository post receive hooks



**fixed bugs**

-   escape url parameters ub UrlBuilder in order to fix \<\<issue
    847\>\>
-   assign revision field in constructor FileObjectWrapper, fix
    \<\<issue 846\>\>
-   IE: Web Interface Only Showing Border and no Login Prompt, see
    \<\<issue 844\>\>
-   fix guice javadoc link



**library updates**

-   update apache shiro to version 1.3.0
-   updated jgit 4.4.0.201606070830-r-scm1, see \<\<issue 848\>\>



**breaking changes**

-   SCM-Manager 1.49 requires at least Java 7

SCM-Manager 1.48
----------------

**improvements**

-   added request uri to mdc logging context
-   added request method to mdc filter
-   log authorization summary to trace level
-   improve logging of AuthorizationCollector
-   improved authorization cache invalidation
-   improve modification events to pass the item before it was modified
    to the subscriber



**fixed bugs**

-   fixed bug in equals method of Permission object
-   fixed syntax highlight for bash/sh, see issue \<\<issue 843\>\>
-   added missing name of repository to access denied exceptions



**library updates**

-   update apache shiro to version 1.2.5

SCM-Manager 1.47
----------------

**improvements**

-   added implmentation for running git gc in an configurable interval,
    see \<\<issue 801\>\>
-   implemented small scheduler engine
-   focus same repository tab as was selected previously, see \<\<issue
    828\>\>
-   added experimental XSRF protection, see \<\<issue 793\>\>
-   Add Jetty host interface variable. Allows to run a specific
    interface or locahost instead of just all interfaces.
-   added option for plugins to change ssl context
-   case insensitive sorting the of name column, see
    [\#PR-25](https://bitbucket.org/sdorra/scm-manager/pull-requests/25/case-insensitive-sorting-of-the-name)



**fixed bugs**

-   removed broken maven repositories
-   do not swallow the ScmSecurityException in PermissionFilter
-   fix order of script resources in production stage, see \<\<issue
    809\>\>
-   JsonContentTransformer should not fail on unknown json properties
-   getCompleteUrl of HttpUtil should now respect forwarding headers,
    see issue \<\<issue 748\>\>
-   fix wrong file permissions as mentioned in \<\<issue 766\>\>
-   update commons-daemon-native to version 1.0.15.1 to fix scm-server
    start on macos



**library updates**

-   update enunciate to version 1.31
-   update jetty to version 7.6.19.v20160209
-   update jersey to version 1.19.1
-   update logback to version 1.1.7
-   update slf4j to version 1.7.21
-   update shiro to version 1.2.4

SCM-Manager 1.46
----------------

**improvements**

-   link modification to files on commit panel, see \<\<issue 356\>\>
-   added to branch switcher to repository browser, see \<\<issue
    355\>\>
-   expose latest changeset id of branch in rest api
-   use cached thread pool to process mercurial process error streams
-   new advanced http client, see \<\<issue 709\>\>



**fixed bugs**

-   fix rendering of bottom toolbar in repository browser, if path is
    null
-   do not show error message for syntax highliting on txt and cs files,
    see \<\<issue 731\>\>
-   fix a bug in git submodule detection
-   fix wrong uft-8 filenames on raw download, see \<\<issue 697\>\>
-   fix missing separator char for post values with same name in http
    client api
-   set content-length header on post requests, see \<\<issue 701\>\>



**library updates**

-   update enunciate to version 1.30.1
-   update jgit to version 3.7.1.201504261725-r-scm1
-   update logback to version 1.1.3
-   update slf4j to version 1.7.12

SCM-Manager 1.45
----------------

**improvements**

-   improve remove repository confirmation dialog
-   introducing HookBranchProvider to get informations about changed
    branches during a hook, see \<\<issue 668\>\>
-   return a changeset property for closed mercurial branches
-   avoid receiving duplicate git commits, during a push with multiple
    new branches
-   retrieve only new git commits, do not collect commits from existing
    branches, see \<\<issue \#663\>\>
-   usability of init script improved.



**fixed bugs**

-   added missing shebang statement in create user script, see \<\<issue
    665\>\>
-   increase timeout for directory import from 30 seconds to 5 minutes,
    to fix \<\<issue \#662\>\>
-   fixed basic authentication with non ascii passwords, see \<\<issue
    627\>\>



**library updates**

-   update jersey to version 1.19
-   update slf4j to version 1.7.10

SCM-Manager 1.44
----------------

**library updates**

-   update jgit to version 3.5.3.201412180710-r in order to fix
    [CVE-2014-9390](http://article.gmane.org/gmane.linux.kernel/1853266)
-   update jersey to version 1.18.3
-   update slf4j to version 1.7.9

SCM-Manager 1.43
----------------

**improvements**

-   new repository import wizard
-   added support for remote urls to push and pull apis (implemented by
    git and hg)
-   added api for bundle/unbundle command to repository apis
    (implemented by svn)
-   implemented AdvancedImportHandler which gives more control over the
    import result than ImportHandler



**fixed bugs**

-   create default accounts only, if no other user exists in the dao
-   do not try to convert git changesets from a receive command of type
    delete, see \<\<issue 634\>\>
-   added utf-8 response encoding for git quick commit view,
    [\#PR-17](https://bitbucket.org/sdorra/scm-manager/pull-request/17/utf-8-response-encoding-for-git-quick)
-   load advanced plugin configuration from plugin directory and from
    root of scm home
-   fix wrong changeset count for git push and pull commands



**library updates**

-   update jersey to version 1.18.2

SCM-Manager 1.42
----------------

**improvements**

-   added comment about POODLE vulnerability to https configuration
-   added extension point for HttpSessionListener
-   implement util class for AutoLoginModules to mark request as
    completed or send redirects from an AutoLoginModule



**fixed bugs**

-   use base uri from request for git quick repository view, see
    \<\<issue 631\>\>



**library updates**

-   update mustache to version 0.8.17
-   update mockito to version 1.10.8

SCM-Manager 1.41
----------------

**improvements**

-   improve performance for simple subversion changeset paging, see
    \<\<issue 610\>\>
-   added cli sub command to generate unique keys
-   added cli sub command to encrypt passwords
-   added mdc logging variables username, client\_ip and client\_host
-   use default jersey client implementation instead of ahc



**fixed bugs**

-   subversion fails to commit filenames containing url-encoded
    character \"/\", see \<\<issue 606\>\>
-   fix some missing file extensions for syntax highlighter
-   fix scm-server stop method on windows, see \<\<issue 623\>\>



**library updates**

-   update jetty to version 7.6.16.v20140903
-   update jgit to version 3.4.1.201406201815-r
-   update mustache to version 0.8.16
-   update svnkit to version 1.8.5-scm2

SCM-Manager 1.40
----------------

**improvements**

-   added display name to web.xml



**fixed bugs**

-   remove url parameters before building base url
-   fixed wrong directory permissions on rpm installations
-   fixed missing preinstall scripts in rpm and deb packages



**library updates**

-   update args4j to version 2.0.28

[Release 1.39 - 1.20](../release%20notes%201.39%20-%201.20/)
