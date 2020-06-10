---
title: Release Notes 1.19 - 1.0
displayToc: true
---

## SCM-Manager 1.19

**improvements**

- implement new template api which uses mustache as default engine
- display error on startup if home directory is not writable
- use cached thread pool for async hooks to improve memory consumption
- added support for jersey viewables
- do not show browser basic authentication dialog on session timeout

**fixed bugs**

- fix npe on windows startup, see \<\<issue 226\>\>

**library updates**

- update javahg to version 0.4
- update enunciate to version 1.26

## SCM-Manager 1.18

**improvements**

- use javahg for mercurial commit, source, blame, content and diff
    views
- support for tags in source view
- support for branches in commit view
- improve svnkit logging, see \<\<issue 211\>\>
- improve mercurial error messages, see \<\<issue 192\>\>
- allow configuration of mercurial repository encoding
- warn if plugin artifact checksum not match

**fixed bugs**

- fix wrong cache result in blame command
- fix wrong escaped subversion commit messages, see \<\<issue 199\>\>
- fix wrong directory content in source browser, see \<\<issue 215\>\>
- fix missing error messages for some json stores
- fix missing localizations
- fix wrong unarchive message
- added missing dtd to server-config.xml

**library updates**

- update ehcache to version 2.6.0
- update jetty to version 7.6.5.v20120716
- update google guava to version 13.0
- update jersey to version 1.13

## SCM-Manager 1.17

**improvements**

- new repository api
- log scm-manager version on boot
- use copy on read for repository api caches to fix reference problems
    with pre processor api
- added api for blame line pre processor
- added compatibility modes for svn 1.7, see \<\<issue 182\>\>
- added warning message if javascript is disabled, see \<\<issue
    178\>\>
- fix ugly login error message, see \<\<issue 183\>\>
- Repository links should use relative paths, see \<\<issue 156\>\>
- Added locale and timezone to support informations

**fixed bugs**

- fix detection of scm-server servlet container
- fix svn version informations
- fix mercurial version informations
- fix mercurial import with non valid mail address in contact field,
    see \<\<issue 173\>\>
- disable ssl validation for mercurial hook detection, see \<\<issue
    170\>\>
- fix basic authentication for systems with turkish locale, see
    \<\<issue 195\>\>

**library updates**

- update jgit to version 2.0.0.201206130900-r
- update svnkit to version 1.7.5-1
- update logback to version 1.0.6
- update slf4j to version 1.6.6

## SCM-Manager 1.16

**improvements**

- improve mercurial hook error handling
- mercurial hook url auto detection
- cleanup empty directories during repository delete, see \<\<issue
    154\>\>
- use urllib2 for urlopen to be more campatible to different python
    versions, see \<\<issue 163\>\>
- redirect to repository root help when accessing repository type root
    url, see \<\<issue 161\>\>
- Add a way to deactivate users, see \<\<issue 153\>\>
- small performance improvements
- store svn uuid as property on repository creation
- allow basic authentication for rest endpoint
    /api/rest/authentication
- added api for store listeners
- added option to encode svn responses with gzip

**fixed bugs**

- do not encode changeset author name, see \<\<issue 160\>\>
- pass shell environment to mercurial cgi process, see \<\<issue
    155\>\>
- fix mercurial encoding problem on windows, see \<\<issue 139\>\>
- fix changing resource order for plugins
- fix repository name validation, see \<\<issue 148\>\>

**library updates**

- update jetty to version 7.6.4.v20120524
- update logback to version 1.0.4
- update jersey-client to version 1.12
- update args4j to version 2.0.21
- update svnkit to version 1.7.4-1.v1

## SCM-Manager 1.15

**improvements**

- added information page for service requests
- templates can be loaded from webapp context or classpath
- allow sourcing in /etc/default/scm-server when available, see
    \<\<issue 145\>\>
- display user informations on the bottom of the page, see \<\<issue
    146\>\>
- improve mercurial error messages, see \<\<issue 138\>\>
- improve logging for plugin loading
- added public and archived option to modify-repository sub command of
    scm-cli-client

**fixed bugs**

- disable jsvc arch call to fix wrong cpu problem on darwin i386
    daemon
- fix wrong german localization, see \<\<issue 122\>\>
- fix build with jdk7
- fix bug in repository name validation, see \<\<issue 142\>\> and
    \<\<issue 144\>\>
- fix subversion path not found on merge, see \<\<issue 136\>\>
- fix subversion lock results in NoSuchMethodError, see \<\<issue
    130\>\>
- fix possible xss in Acitivities View, Repository Commits, see
    \<\<issue 131\>\>
- fix plugin installation failed with enabled proxy server, see
    \<\<issue 124\>\>
- fix wrong changeset url in ui url provider

**library updates**

- update google guava to version 12.0
- update logback to version 1.0.3

## SCM-Manager 1.14

**improvements**

- archive for repositories, see \<\<issue 42\>\>
- simpler api to create other backend as the default xml version
- api for overriding core components
- extensionpoint for ServletContextListener
- improved error dialog in user interface, see \<\<issue 107\>\>
- improve german locale

**fixed bugs**

- fix wrong log messages
- fix repository creation failure leaves empty directory, see
    \<\<issue 106\>\>
- fix mercurial repository source does not display with
    subrepositories, see \<\<issue 104\>\>
- fix history repository selection

**library updates**

- update jetty to version 7.6.3.v20120416
- update logback to version 1.0.1
- update ehcache to version 2.5.2
- update commons-daemon to version 1.0.10, see \<\<issue 103\>\>

## SCM-Manager 1.13

**improvements**

- more robust python modules to access mercurial resources
- added disable option to each core plugin

**fixed bugs**

- fix changing etags for collections
- fix missing hooks of mercurial repository import, see \<\<issue
    97\>\>
- fix anonymous push to public mercurial repositories, see \<\<issue
    97\>\>
- execute \"hg init\" in the scm home directory instead of current
    working directory, see \<\<issue 97\>\>
- use repository directory as working directory for git repository
    hooks, see \<\<issue 99\>\>
- do not fail on non basic authorization header
- fix mercurial encoding problem, see \<\<issue 95\>\>
- fix issue-94 can\'t edit users, see \<\<issue 94\>\>

**library updates**

- update freemarker to version 2.3.19
- update jetty to version 7.6.1.v20120215
- update jgit to version 1.3.0.201202151440-r
- update jersey to version 1.12

## SCM-Manager 1.12

**improvements**

- new theme
- support for mercurial 2.1
- import of existing repositories, see \<\<issue 59\>\>
- new changeset view
- show categories in plugin overview
- added api for project stages
- added api to fetch a single changeset

**fixed bugs**

- allow usernames \< 3 chars, see pull request 2
- git: use author ident instead of commit ident
- fix plugin resource caching bug
- mercurial: fix getChangesets of post receive hooks
- fix history bug during repository selection

**library updates**

- update jetty to version 7.6.0.v20120127
- update maven for aether to version 3.0.4
- update ehcache to version 2.5.1

##SCM-Manager 1.11

**improvements**

- allow to fetch repositories by type and name
- added icon for tags
- show parent revision in changeset viewer
- added repository browser support for external git submodules

**fixed bugs**

- fix wrong svn diff
- fix wrong revision for sub module repository browser
- fix basic authentication access with a colon in the user password,
    see \<\<issue 88\>\>
- fix git hooks for repository structures
- fix subversion hooks on windows with repository structure
- fix mercurial hooks on windows with repository structure
- fix wrong mercurial revisions in urls
- fix svn hooks for repositories located on soft links
- fix bug in appendParameter method of UrlBuilder
- truncate long svn status lines, see \<\<issue 83\>\>

## SCM-Manager 1.10

**improvements**

- small performance improvements
- added basic support for external mercurial subrepositories
- added repository request listener api
- added file object pre processor api

**fixed bugs**

- fix vertical scrollbar in webkit based browsers
- fix mercurial push with symbolic links, see \<\<issue 82\>\>
- fix wrong decoding in hgweb, see \<\<issue 79\>\>
- fix plugin installation with a proxy server, see \<\<issue 76\>\>
- fix \"Allow anonymous access\" breaks access to private
    repositories, see \<\<issue 77\>\>

**library updates**

- update args4j to version 2.0.19
- update aether to version 1.13.1
- update jersey to version 1.11
- update jgit to version 1.2.0.201112221803-r
- update svnkit to version 1.3.7.1

## SCM-Manager 1.9

**improvements**

- Support for directory structure, see \<\<issue 47\>\>
- Added webservice method to fetch repository by its type and name
- Mercurial auto configuration support for homebrew installations
- Improve httpclient api to support headers and authentication
- Reimplemented browser history functions
- SCM-Manager is now complete bookmark-able
- Added api to create urls for the interface or the webservice
- Improve interface performance by reducing Ext.getCmp calls
- Added history panel for a single file, see pull request 1
- Added wiki categories, wiki and screenshots to plugin descriptor
- Added version and server-version to scm-cli-client
- Improve performance by better repository caching

**fixed bugs**

- fix bug in get method of repository resource
- fix issue \"Administrator flag is disabled after login\", see
    \<\<issue 73\>\>
- Allow usernames with spaces, see \<\<issue 69\>\>
- fix rolling file policy of scm-manager logging
- fix mercurial hooks with configured force base url
- fix mercurial hooks with apache mod\_proxy, see \<\<issue 71\>\>

**library updates**

- update jersey to version 1.10
- update slf4j to verion 1.6.4
- update logback to version 1.0.0
- update jetty to version 7.5.4.v20111024

## SCM-Manager 1.8

**improvements**

- support for pre receive hooks, see \<\<issue 62\>\>
- scm-maven-plugin are now useable for integration tests
- added readme file to server bundles
- support for mercurial 1.9
- improve mercurial performance
- support for blame/annotation view, special thanks to narva.com
- support for Diff views of changesets
- added basic authentication support to restful webservice
- generate webservice documentation
- improve javadoc
- download for single files from repository

**fixed bugs**

- Fixed bug in git repositories without head

**library updates**

- update jetty to version 7.5.1.v20110908
- update aether to version 1.13
- update ehcache to version 2.4.5
- update logback to version 0.9.30
- update jgit to version 1.1.0.201109151100-r
- update jersey to version 1.9

## SCM-Manager 1.7

**improvements**

- added repository search and repository type filter to user
    interface, see \<\<issue 48\>\>
- handle browser back and forward buttons
- improve output of mercurial hooks
- added injection support for authentication, group, repository, user
    and hook listeners
- added cipher api
- select new repository after creation
- added option to configure the scm home directory with a classpath
    resource, see \<\<issue 53\>\>
- support for proxyservers with authentication, see
    [ko7eGU88rB4](https://groups.google.com/forum/#!topic/scmmanager/ko7eGU88rB4 "Plugins through http auth proxy?")
- improve changeset pre processor api
- improve support for repository, group and user properties

**fixed bugs**

- show installed plugins without internet connection, see \<\<issue
    44\>\>
- fix git svn clone, see \<\<issue 45\>\>
- fix wron chanllenge bug in mercurial hook management
- fix jdk7 build bug
- fix a classloading problem in scmp:run maven goal
- fix bug in property xml serialization

**library updates**

- update ehcache to version 2.4.4
- update slf4j to version 1.6.2
- update junit to version 4.9

## SCM-Manager 1.6

**note**

- GlassFish 3.x users have to change their GlassFish configuration,
    see [SCM-Manager with GlassFish](../applicationserver/#glassfish)

**improvements**

- added an api for repository hooks
- support for git post-receive hook
- improved performance for source and commit viewer
- added loading mask of commit viewer
- added run as admin api
- improved validaton of user, group and repository names
- simplify plugin development
- added icons to member and permission grid
- added properties to repositories, users and groups

**fixed bugs**

- fixed deployment bug on some tomcat configurations, \<\<issue 38\>\>
- fixed NullPointerException with empty git repositories, \<\<issue
    36\>\>
- fixed source and commit browser for git repositories without HEAD
    ref
- fixed missing updates in plugin overview
- fixed plugin update
- fixed xml representation of changeset webservice resource
- fixed error 500 in mercurial repository browser with configured
    python path

**library updates**

- upgraded jetty to version 7.4.5.v20110725
- upgraded wagon to version 1.0
- upgraded ehcache to version 2.4.3
- upgraded aether to version 1.12

## SCM-Manager 1.5

**note**

- mod\_proxy users have to change their configuration, see [mod\_proxy configuration](../apache/apache-mod_proxy)

**improvements**

- added a \"repositorybrowser\"
- added missing error messages on session timeout
- support for mod\_proxy forward with ssl, see \<\<issue 32\>\>
- added toolbar icons for add, remove and refresh action

**fixed bugs**

- fixed javascript error on loginwindow cancelbutton click
- fixed bug with non default git repository directory, see \<\<issue
    29\>\>

**library updates**

- upgraded jersey to version 1.8
- upgraded jgit to version 1.0.0.201106090707-r

## SCM-Manager 1.4

**improvements**

- support for IE 9
- using tabs for group details

**fixed bugs**

- fixed svn move command, see \<\<issue 25\>\>
- fixed svn log command, see \<\<issue 25\>\>

**library updates**

- upgraded jersey-ahc-client to version 1.0.2
- upgraded jersey-client to version 1.7
- upgraded logback to version 0.9.29
- upgraded extjs to version 3.4.0

## SCM-Manager 1.3

**improvements**

- added a client api
- added a commandline client
- added support for proxy servers, see \<\<issue 14\>\>
- improved plugin page
- improved session timeout handling, see \<\<issue 23\>\>
- move logging configuration to \"conf\" folder of scm-server

**fixed bugs**

- svn: fixed wrong content-length in DAVServlet, see \<\<issue 24\>\>
- svn: fixed wrong encoding, see \<\<issue 22\>\>

**library updates**

- upgraded jetty to version 7.4.2.v20110526
- upgraded google guice to version 3.0
- upgraded ehcache to version 2.4.2
- upgraded freemarker to version 2.3.18
- upgraded jersey to version 1.7

## SCM-Manager 1.2

**improvements**

- added a \"changesetviewer\"
- using tabs for repository configuration
- added a configuration wizard for mercurial
- the date format is now configurable
- added a repository information panel
- new cgi api
- added subversion compatibility switches, see \<\<issue 13\>\>

**fixed bugs**

- fixed ssl support in scm-server, see \<\<issue 9\>\>
- fixed ssl support in mercurial cgi servlet, see \<\<issue 9\>\>
- fixed a browser window resize bug, see \<\<issue 10\>\>
- fixed bug with spaces in the scm home path, see \<\<issue 11\>\>

**library updates**

- upgrade freemarker to version 2.3.16
- upgrade jersey to version 1.6
- upgrade ehcache to version 2.4.1
- upgrade jgit to version 0.12.1

## SCM-Manager 1.1

- Support for Unix-Daemons and Windows-Services
- Support for localization
- German localization
- Help tooltips
- New Plugin-Backend
