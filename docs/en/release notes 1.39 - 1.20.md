---
title: Release Notes 1.39 - 1.20
displayToc: true
---

SCM-Manager 1.39
----------------

**improvements**

-   created rpm and deb packages for scm-server
-   improve error handling of permission system
-   set DefaultCGIExecutor logger level to debug instead of trace
-   added tcpip dependency for scm-server windows service
-   implemented health checks for mercurial and git repositories
-   implemented marker interface for plugin class loaders to make it
    easier to find class loader leaks



**fixed bugs**

-   fix mailto links
-   fix automatic start as service on windows server 2012, see \<\<issue
    \#349\>\>
-   fix open webserver port \< 1024 as non privileged user
-   encode user and group names for rest requests, see \<\<issue
    \#591\>\>
-   translate path for scmp plugin installation, see \<\<issue \#586\>\>
-   remove antiJARLocking attribute from context.xml, because it is no
    longer supported by tomcat 8
-   fix possible class loader leak
-   fix IndentXMLStreamWriterTest on windows
-   fix marshalling exception on plugin installation with rest api, see
    \<\<issue \#578\>\>



**library updates**

-   update mustache to version 0.8.15
-   update jgit to version 3.4.0.201406110918-r
-   update commons-beanutils to version 1.9.2
-   update commons-daemon to version 1.0.15

SCM-Manager 1.38
----------------

**fixed bugs**

-   fix NoClassDefFoundError in scm-cli-client, see issue \<\<issue
    \#576\>\>
-   escape backslash in checkout url, see \<\<issue \#570\>\>
-   fixed a circular guice dependency
-   do not use subject \"run as\" for administration context, because it
    could affect other threads
-   fix a linkage error on Util.nonNull, see \<\<issue \#569\>\>
-   fix wrong date format in logging configuration

SCM-Manager 1.37
----------------

**improvements**

-   improved git error messages for failed authentication and not enough
    permissions
-   improve error handling for failed authentication and not enough
    privileges
-   added date to log pattern
-   update last modified date of a repository after each push
-   added hidden last modified column to repository grid



**fixed bugs**

-   resolve dependency resolution conflicts, see \<\<issue 541\>\>,
    \<\<issue 549\>\> and \<\<issue 558\>\>
-   fix basic authentication for urls which contain a username but
    without password, see \<\<issue 545\>\>
-   subversion repositories are not closed correctly, see \<\<issue
    554\>\>
-   use a more robust check if html5 localStorage is available, see
    \<\<issue 548\>\>
-   subversion cannot delete properties, see \<\<issue 547\>\>



**library updates**

-   update jetty to version 7.6.15.v20140411
-   update svnkit to version 1.8.5-scm1
-   update jgit to version 3.3.2.201404171909-r
-   update logback to version 1.1.2
-   update slf4j to version 1.7.7
-   update commons-code to version 1.9

SCM-Manager 1.36
----------------

**improvements**

-   added feature to set custom realm description, see [PR
    16](https://bitbucket.org/sdorra/scm-manager/pull-request/16/add-feature-to-set-custom-realm)
-   added option to skip failed authenticators
-   cli-client: allow retrieving repositories by using type/name instead
    of repository id
-   implemented repository health checks



**fixed bugs**

-   solve classloading issues for plugin classes
-   fix changing passwords which a shorter than 5 chars, see issue
    \<\<issue 535\>\>
-   fix possible npe on authentication, see issue \<\<issue 531\>\>
-   fix exception on login, if an external authenticator returns a
    changed user object
-   basic auth filter should return 403 instead of 401 for wrong
    credentials, see issue \<\<issue 520\>\>
-   fix bug with passwords which contains a colon, see \<\<issue 516\>\>
-   fix double slash for append and getCompleteUrl of HttpUtil
-   fix bug with some special chars in SearchUtil
-   fix mercurial out of scope exception on startup



**library updates**

-   update args4j 2.0.26
-   update svnkit to version 1.8.4-scm1
-   update jgit to 3.3.0.201403021825-r
-   update shiro to version 1.2.3
-   update jersey to version 1.18.1
-   update logback to version 1.1.1
-   update slf4j to version 1.7.6
-   update commons-beanutils to version 1.9.1

SCM-Manager 1.35
----------------

**improvements**

-   Spanish translation, see [PR
    9](https://bitbucket.org/sdorra/scm-manager/pull-request/9/spanish-translation)
    thanks to [Ángel L.
    García](https://bitbucket.org/algarcia)
-   added auto-login filter system, see [PR
    4](https://bitbucket.org/sdorra/scm-manager/pull-request/4/modifications-for-auto-login)
    thanks to [Clemens Rabe](https://bitbucket.org/seeraven)
-   added property to disable escaping for blame, browse, log command
    and hooks, see \<\<issue 505\>\>
-   remove cancel button on login window if anonymous access is
    disabled, see \<\<issue 396\>\>
-   syntax highlighting for c, h, hh and cc files, see [PR
    11](https://bitbucket.org/sdorra/scm-manager/pull-request/11/c-java-extensions-added-to)
    thanks to [pankaj azad](https://bitbucket.org/pankajazad)
-   custom subversion collection renderer
-   use full message instead of short message for git commits, see
    \<\<issue 474\>\>
-   improved german translation, see [PR
    5](https://bitbucket.org/sdorra/scm-manager/pull-request/5/german-translation-adjusted/diff)
    thanks to [Ahmed Saad](https://bitbucket.org/saadous)
-   use same validation rules for user and group names, see \<\<issue
    470\>\>
-   added brushes for applescript and sass



**fixed bugs**

-   fixed file leak, see \<\<issue 500\>\>
-   fixed double escaping, see \<\<issue 505\>\>
-   fixed python path for scm mercurial packages, see \<\<issue 499\>\>
-   remove setContentLength with -1 to fix CGI on jetty 9, see \<\<issue
    481\>\>
-   retry delete up to 5 sec to fix problem with windows locking, see
    \<\<issue 476\>\>
-   fix wrong commit and source url on git repositories with reverse
    proxy, see \<\<issue 483\>\>
-   use work directory instead of temp directory for scm-manager webapp,
    see \<\<issue 464\>\>
-   fix wrong sql brush filename, see \<\<issue 461\>\>



**library updates**

-   update jetty to 7.6.14.v20131031
-   update jersey to 1.18
-   update svnkit to 1.7.10-scm4
-   update jgit to 3.2.0.201312181205-r
-   update enunciate to 1.28
-   update mustasche to 0.8.14
-   update javahg to 0.7

SCM-Manager 1.34
----------------

**improvements**

-   allow multi line mercurial messages
-   change order of permission column and use a more robust cell editor
    check
-   added api for changing diff output format
-   added support for glassfish 4, see \<\<issue 435\>\>
-   added configuration changed event
-   implement login attempt handler to handle failed authentications



**fixed bugs**

-   fix bug with user events and decorated user managers
-   fix hg push fails with \'URLError\' object has no attribute
    \'read\', see \<\<issue 424\>\>
-   fix CommandNotSupportedException for git outgoing command
-   fix detection of non bare repositories as pull source
-   fix scm behind reverse proxy on root: svn not working, see \<\<issue
    436\>\>
-   fix permission caching for logged in users
-   fix possible npe with unpacked war files, see \<\<issue 440\>\>
-   fix escaping bug in SearchUtil, see \<\<issue 441\>\>
-   avoid duplicate members in groups, see \<\<issue 439\>\>
-   fix store and load method of xml configuration entry store
-   fix out of scope exception on access hgcontext, see \<\<issue
    451\>\>



**library updates**

-   update jetty to 7.6.13.v20130916
-   update guava to version 15.0

SCM-Manager 1.33
----------------

**improvements**

-   added api to bypass changeset pre processors during hook
-   added api to send messages back to scm client during hook
-   create scm-client-impl jar with all dependencies
-   introduce Event annotation to mark event classes which are
    receiveable over the event system



**fixed bugs**

-   fix login window scrollbars on linux with firefox 21, see \<\<issue
    411\>\>
-   fix name resolution for git repositories with working copy, see
    \<\<issue 415\>\>
-   fix handling of \"svn lock\", see \<\<issue 420\>\>
-   fix handling of \"svn copy\", see \<\<issue 422\>\>
-   clear authorization cache, when a group has changed, see \<\<issue
    423\>\>
-   fix wrong svn hook error messages
-   fix wrong sytem account e-mails, use scm-manager.org instead of
    scm-manager.com
-   implement svn cat during pre receive repository hooks
-   fix handling of pending changesets during pre receive repository
    hooks



**library updates**

-   update jgit to 3.0.0.201306101825-r
-   update args4j to version 2.0.25
-   update freemarker to version 2.3.20
-   update enunciate to version 1.27
-   update ehcache to version 2.6.6
-   update to svnkit 1.7.10-scm3
-   update mustache to version 0.8.13

SCM-Manager 1.32
----------------

**improvements**

-   added support for subversion 1.8 and ra\_serf (\<\<issue 222\>\>,
    \<\<issue 406\>\>)
-   added detection eclipse jetty (standalone) to
    ServletContainerDetector (pull request 3)



**fixed bugs**

-   fix loading of cache configurations from plugins
-   resolve conflicts for plugins and plugin dependencies
-   fix parsing of security.xml on older jre\'s (\<\<issue 405\>\>)
-   fix source, commit, etc. views of mercurial on systems were the home
    directory is not writable (\<\<issue 398\>\>)
-   fix wrong python path on mercurial homebrew installations

SCM-Manager 1.31
----------------

**improvements**

-   added first access url and credentials to readme
-   option to assign global access permissions to users and groups
    (\<\<issue 340\>\>)
-   store api for multiple configuration entries
-   added group for all authenticated users
-   implementation of a remember me system (\<\<issue 384\>\>)
-   implment incoming, outgoing, push and pull command for git and
    mercurial
-   display repository access permissions on info panel (\<\<issue
    364\>\>))
-   improve plugin archetype and use version 1.23 of scm-manager as
    parent
-   create and deploy package for rest documentation



**fixed bugs**

-   fix wrong message for deleting repositories (\<\<issue 370\>\>)
-   fix button handling on repository grid with enabled archive mode
    (\<\<issue 372\>\>)
-   fix missing git index view (\<\<issue 377\>\>)
-   ignore global proxy settings for mercurial callback hooks (\<\<issue
    376\>\>)
-   fix registration of synchronous event handlers
-   fix classpath generation with manually installed plugins (\<\<issue
    395\>\>)
-   fix daemon mode on some operating systems (\<\<issue 397\>\>)



**library updates**

-   update logback to version 1.0.13
-   update svnkit to version 1.7.9-scm1
-   update jetty to 7.6.11.v20130520
-   update web-compressor to version 1.5
-   update mustache to version 0.8.12
-   update javahg to version 0.6
-   update apache shiro to version 1.2.2

SCM-Manager 1.30
----------------

**fixed bugs**

-   fix missing copy strategy in guava cache configuration

SCM-Manager 1.29
----------------

**improvements**

-   use guava as default cache implementation (\<\<issue 345\>\>)
-   merge cache configuration from default location, config directory
    and plugins (\<\<issue 345\>\>)
-   create a copy of tag collections to reduce memory on caching
    (\<\<issue 345\>\>)
-   added configuration for authorization cache (\<\<issue 345\>\>)
-   default authentication handler should always be the first in the
    authentication chain
-   improve logging of BootstrapUtil
-   implemented a child first plugin classloader strategy
-   use template engine and repository service for git repository page
    (\<\<issue 341\>\>)



**fixed bugs**

-   synchronize getCache method of cache manager implementations
    (\<\<issue 345\>\>)
-   create a copy of search result collection to reduce memory of caches
    (\<\<issue 345\>\>)
-   send mercurial hook error messages to client (\<\<issue 333\>\>)
-   use content type text/html for mercurial error messages, if the
    client accept it (\<\<issue 336\>\>)
-   scm-svn-plugin does not handle modified paths on pre-receive hooks
    (\<\<issue 353\>\>)
-   use a initial capacity of one for subversion hook changesets
-   fix wrong handling of git file hooks (\<\<issue 339\>\>)
-   sonia.scm.net.HttpRequest.appendValues() adds parameter values twice
    (\<\<issue 342\>\>)



**library updates**

-   update ehcache to version 2.6.5
-   update jersey to version 1.17.1
-   update guava to version 14.0.1
-   update logback to version 1.0.11
-   update slf4j to version 1.7.5
-   update mustache to version 0.8.11
-   update jgit to version 2.3.1.201302201838-r
-   update maven-aether-provider to version 3.0.5

SCM-Manager 1.28
----------------

**improvements**

-   added scm.home example for windows, see \<\<issue 328\>\>
-   disable directory listings on default scm-server configuration
-   respect subscriber annotation on event bus registration
-   register every injectable object to event bus
-   enable tab scrolling for repository setting tabs
-   use async cache for scm realm
-   improve manager exception handling



**fixed bugs**

-   fix path traversal vulnerability in git changelog api, see \<\<issue
    319\>\>
-   fix possible crlf injections, see \<\<issue 320\>\>
-   fix admin access vulnerability in user repository creation, see
    \<\<issue 331\>\>
-   fix circular proxy error on binding
-   protect mustache resources
-   fix eager singleton loading

SCM-Manager 1.27
----------------

**improvements**

-   exclude commons-logging and use jcl-over-slf4j instead
-   icons of repository browser should be clickable
-   post authentication events to the new event system



**fixed bugs**

-   fix binding of extensions with eager singleton scope
-   fix bug with registration of multiple authentication listeners
-   fix localStorage detection for ie 6 and 7
-   fix hover links for ie \>= 8, see \<\<issue 317\>\>

SCM-Manager 1.26
----------------

**improvements**

-   use localStorage to store state of the user interface
-   improve logging of plugin installer
-   find and bind extension points automatically
-   added option to disable the last commit for browse command
-   added recursive option to browse command
-   added option to disable sub repository detection of browse command



**fixed bugs**

-   normalize urls for BaseUrlFilter to prevent redirect loops, see
    \<\<issue 311\>\>
-   fix privileged action is not executed, if the user is already an
    admin
-   added missing id for security navigation section
-   synchronize getChangeset method of hook events and call registered
    pre processors before the changesets are returned to hook



**library updates**

-   update jersey to version 1.17

SCM-Manager 1.25
----------------

**improvements**

-   added feature api for specific repository types
-   improve logging of plugin installer



**fixed bugs**

-   fix svn make and put with Polish characters in path, see \<\<issue
    298\>\>
-   fix bookmarkable support for ie, see \<\<issue 297\>\>
-   call ui repository open listener, no matter which permission the
    user has
-   fix IllegalArgumentException with nested privileged actions
-   fix installing plugin package breaks classpath.xml, see \<\<issue
    306\>\>



**library updates**

-   update svnkit to version 1.7.8-scm1
-   update ehcache to version 2.6.3
-   update mustache to version 0.8.9
-   update jgit to version 2.2.0.201212191850-r
-   update enunciate to version 1.26.2

SCM-Manager 1.24
----------------

**fixed bugs**

-   fix wrong default date format

SCM-Manager 1.23
----------------

**improvements**

-   new event api based on guavas EventBus
-   added option to exclude hosts from proxy, see \<\<issue 281\>\>
-   set name for different Threads to simplify debugging
-   added eager singleton scope for injection
-   added blob store api, to store unstructured data
-   added data store api, to store structured data
-   added decorator api for manager objets
-   use moment.js to format dates in ui
-   use javahg to retrieve changesets from a mercurial hook
-   prepare server-config.xml for request logging
-   improve javadoc



**fixed bugs**

-   use system environment when executing \"hg create\"
-   fix build from source, see \<\<issue 289\>\>
-   svn mergeinfo returns wrong results, see \<\<issue 280\>\>
-   svn diff fails if the path contains spaces, see \<\<issue 282\>\>
    and \<\<issue 290\>\>
-   BasicPropertiesAware should be implement Serializable
-   changeset.id for mercurial changesets should always return a
    complete node id, \<\<issue 287\>\>
-   fix mercurial sub repository detection in source browser
-   fix non closing client response



**library updates**

-   update selenium to version 2.28.0
-   update svnkit to version 1.7.6-scm3
-   update logback to version 1.0.9
-   update junit to 4.11
-   update jetty to version 7.6.8.v20121106
-   update ehcache to version 2.6.2
-   update javahg to version 0.5
-   update jersey to version 1.16

SCM-Manager 1.22
----------------

**improvements**

-   store expanded/collapsed state of groupingviews across sessions, see
    \<\<issue 268\>\>
-   added favicon and new logo
-   added method to read templates from a reader
-   added repository type icons to grid



**fixed bugs**

-   fix permission autocomplete, see \<\<issue 267\>\>



**library updates**

-   update mustache.java to version 0.8.8
-   update mockito to version 1.9.5

SCM-Manager 1.21
----------------

**improvements**

-   reimplment the complete security model on top of apache shiro
-   allow execution of administration tasks without an active http
    session
-   use shorter repository ids
-   added option to install plugin packages
-   added option to display mercurial revisions as part of the node id,
    see \<\<issue 251\>\>
-   improve performance and memory consumption of svn log command
-   do not log sensitive cgi env variables



**fixed bugs**

-   fix freezing configuration form on ie, see \<\<issue 236\>\>
-   fix wrong branch informations of git repository hooks, \<\<issue
    242\>\> and \<\<issue 235\>\>
-   fix bug in history of subversion repositories
-   fix wrong mercurial changeset ids during hooks



**library updates**

-   update google guava to version 13.0.1
-   update jetty to version 7.6.7.v20120910
-   update jersey to version 1.14
-   update args4j to version 2.0.22
-   update jgit to 2.1.0.201209190230-r
-   update enunciate to version 1.26.1
-   update mustache to version 0.8.7
-   update slf4j to version 1.7.2

SCM-Manager 1.20
----------------

**improvements**

-   added java.awt.headless system property to server startup scripts
-   strip changeset ids to 12 chars
-   use eternal caches for new repository api
-   added placeholder to commit view



**fixed bugs**

-   fix non closing \"hg serve\" processes
-   fix error on changing branches in commit viewer
-   fix wrong file modifications on git changeset overview



**library updates**

-   update logback to version 1.0.7

[Release 1.19 - 1.0](../release%20notes%201.19%20-%201.0/)
