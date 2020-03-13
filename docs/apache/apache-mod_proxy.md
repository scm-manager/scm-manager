SCM-Server and Apache mod\_proxy
--------------------------------

### Apache configuration

-   -   Warning\*\*: Setting ProxyPassReverseCookiePath would most
        likely cause problems with session handling!

<!-- -->

-   -   Note\*\*: If you encounter timeout problems, please have a look
        [here](http://httpd.apache.org/docs/current/mod/mod_proxy.html#workers "wikilink").

### SCM-Server conf/server-config.xml

NOTE: This file is found in the installation directory, not the user\'s
home directory.

Uncomment following line: Example: === SCM-Manager Configuration version
1.5 and above ==

1.  Login as an admin user and select \"General\"
2.  Set the \"Base Url\" to the URL of the Apache (\*\*warning:\*\*
    don\'t check \"Force Base Url\")
3.  Save the new new settings

### SCM-Manager Configuration before version 1.5

1.  Login as an admin user and select \"General\"
2.  Set the Serverport to the apache port (normally port 80)
3.  Save the new settings
