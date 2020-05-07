---
title: SCM-Server and Apache mod_proxy
---

### Apache configuration

```apache
ProxyPass /scm http://localhost:8080/scm
ProxyPassReverse /scm http://localhost:8080/scm
ProxyPassReverse  /scm  http://servername:8080/scm
<Location /scm>
 Order allow,deny
 Allow from all
</Location>
```
- **Warning**: Setting ProxyPassReverseCookiePath would most likely cause problems with session handling!
- **Note**: If you encounter timeout problems, please have a look at [Apache Module mod_proxy#Workers](http://httpd.apache.org/docs/current/mod/mod_proxy.html#workers).

### SCM-Server conf/server-config.xml

NOTE: This file is found in the installation directory, not the user\'s
home directory.

Uncomment following line: 
```xml
<Set name="forwarded">true</Set>
```

Example: 
```xml
<Call name="addConnector">
  <Arg>
    <New class="org.eclipse.jetty.server.nio.SelectChannelConnector">
      <Set name="host">
        <SystemProperty name="jetty.host" />
      </Set>
      <Set name="port">
        <SystemProperty name="jetty.port" default="8080"/>
      </Set>
      <!-- for mod_proxy -->
      <Set name="forwarded">true</Set>
    </New>
  </Arg>
</Call>
```

### SCM-Manager Configuration version 1.5 and above

1. Login as an admin user and select \"General\"
2. Set the \"Base Url\" to the URL of the Apache (**warning:** don\'t check \"Force Base Url\")
3. Save the new new settings

### SCM-Manager Configuration before version 1.5

1. Login as an admin user and select \"General\"
2. Set the Serverport to the apache port (normally port 80)
3. Save the new settings
