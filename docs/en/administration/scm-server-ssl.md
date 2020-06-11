---
title: SCM-Server SSL
---

<!--
TODO: Update 
Node: https://ssl-config.mozilla.org/#server=jetty&version=9.4.28&config=intermediate&guideline=5.4
-->

**Note**: This document describes a ssl configuration with a
        self-signed certificate

1\. Open a shell and go to the conf directory of the scm-server

2\. Create a certificate request. Replace all variables (\*varname\*) 

```bash
$ keytool -genkey -alias scm -keyalg RSA -keystore keystore.jks

Enter keystore password: your password
Re-enter new password: your password
What is your first and last name?
  [Unknown]:  *your servername*
What is the name of your organizational unit?
  [Unknown]:  *organisation unit*
What is the name of your organization?
  [Unknown]:  *organisation*
What is the name of your City or Locality?
  [Unknown]:  *city*
What is the name of your State or Province?
  [Unknown]:  *state*
What is the two-letter country code for this unit?
  [Unknown]:  *country code*
Is CN=your servername, OU=your organisation unit, O=your organisation, L=your city, ST=your state, C=cc correct?
  [no]:  yes

Enter key password for <scm>
	(RETURN if same as keystore password): *password*
Re-enter new password: *password*
```

**Note**: You have to enter the full qualified hostname of your
        server for the cn (cn = What is your first and last name?)

3\. Edit the server-config.xml, uncomment the SSL-Connector and set your
password. For example:

```xml
<Call name="addConnector">
  <Arg>
    <New class="org.eclipse.jetty.server.ssl.SslSelectChannelConnector">
       <Arg>
       <!--
       Exclude SSLv3 to avoid POODLE vulnerability.
       See https://groups.google.com/d/msg/scmmanager/sX_Ydy-wAPA/-Dvs5i7RHtQJ
        -->
         <New class="org.eclipse.jetty.http.ssl.SslContextFactory">
           <Set name="excludeProtocols">
             <Array type="java.lang.String">
               <Item>SSLv2Hello</Item>
               <Item>SSLv3</Item>
             </Array>
           </Set>
         </New>
      </Arg>
      <Set name="Port">8181</Set>
      <Set name="maxIdleTime">30000</Set>
      <Set name="keystore"><SystemProperty name="basedir" default="." />/conf/keystore.jks</Set>
      <Set name="password">*password*</Set>
      <Set name="keyPassword">*password*</Set>
      <Set name="truststore"><SystemProperty name="basedir" default="." />/conf/keystore.jks</Set>
      <Set name="trustPassword">*password*</Set>
    </New>
  </Arg>
</Call>
```

4\. Start or restart the scm-server

**Note**: It looks like there is a error in some version of
        OpenJDK (issues \#84 and \#151). If you have such a problem,
        please try to use the Oracle JDK.

### Configure Git

1\. Export the certificate from keystore: 

```bash
$ keytool -exportcert -keystore keystore.jks -alias scm -rfc -file cert.pem
```

2\. Copy the certificate to your client and add it to your git config: 

```bash
$ git config http.sslCAInfo /complete/path/to/cert.pem
```

### Configure Mercurial

1\. Export the certificate from keystore: 

```bash
$ keytool -exportcert -keystore keystore.jks -alias scm -rfc -file cert.pem
```

2\. Copy the certificate to your client and add it to your .hgrc config
file: 

```bash
[web]
cacerts = /complete/path/to/cert.pem
```

### Sources

- [Keytool](http://download.oracle.com/javase/1.4.2/docs/tooldocs/windows/keytool.html)
- [Jetty SSL-Connectors](http://wiki.eclipse.org/Jetty/Reference/SSL_Connectors)
