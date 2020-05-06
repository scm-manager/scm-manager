---
title: SCM-Server and Nginx
---

## Nginx configuration

```text
location /scm {
  proxy_set_header X-Real-IP         $remote_addr;
  proxy_set_header X-Forwarded-For   $proxy_add_x_forwarded_for;
  proxy_set_header X-Forwarded-Proto $scheme;
  proxy_set_header Host $http_host;
  proxy_pass       http://localhost:8080;
}
```

## SCM-Server conf/server-config.xml

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

## SCM-Manager Configuration version 1.5 and above
* Login as an admin user and select "General"
* Set the "Base Url" to the URL of Nginx (**warning:** don't check "Force Base Url")
* Save the new new settings
