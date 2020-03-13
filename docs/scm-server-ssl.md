SCM-Server SSL
--------------

-   -   Note\*\*: This document describes a ssl configuration with a
        self-signed certificate

1\. Open a shell and go to the conf directory of the scm-server

2\. Create a certificate request. Replace all variables (\*varname\*) 

-   -   Note:\*\* You have to enter the full qualified hostname of your
        server for the cn (cn = What is your first and last name?)

3\. Edit the server-config.xml, uncomment the SSL-Connector and set your
password. For example:

4\. Start or restart the scm-server

-   -   Note\*\*: It looks like there is a error in some version of
        OpenJDK (issues \#84 and \#151). If you have such a problem,
        please try to use the Oracle JDK.

### Configure Git

1\. Export the certificate from keystore: 

2\. Copy the certificate to your client and add it to your git config: 

### Configure Mercurial

1\. Export the certificate from keystore: 

2\. Copy the certificate to your client and add it to your .hgrc config
file: 

### Sources

-   [Keytool](http://download.oracle.com/javase/1.4.2/docs/tooldocs/windows/keytool.html "wikilink")
-   [Jetty
    SSL-Connectors](http://wiki.eclipse.org/Jetty/Reference/SSL_Connectors "wikilink")
