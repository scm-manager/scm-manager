---
title: Reverse Proxy
subtitle: How to use SCM-Manager with common reverse proxies
displayToc: true
---

TODO reverse proxies in general send X-Forwarded headers ...

### nginx

TODO ...

### Apache

<!--
TODO: does this set X-Forwarded Headers?
-->

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

### HA-Proxy

TODO ...

### SCM-Server conf/server-config.xml

<!--
TODO: do we need it
-->

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
