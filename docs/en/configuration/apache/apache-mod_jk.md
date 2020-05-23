---
title: SCM-Server and Apache mod_jk
---

### Apache Configuration
```apache
JkWorkersFile /etc/apache2/jkworkers.properties
JkLogFile     /var/log/apache2/mod_jk.log
JkLogLevel    info

JkMount  /scm* worker1
```

### JK Workers File (jkworkers.properties)
```ini
worker.list=worker1
worker.worker1.type=ajp13
worker.worker1.host=localhost
worker.worker1.port=8009
worker.worker1.lbfactor=50
worker.worker1.cachesize=10
worker.worker1.cache_timeout=600
worker.worker1.socket_keepalive=1
```

### SCM-Server conf/server-config.xml
Uncomment the following lines:
```xml
<Call name="addConnector">
 <Arg>
   <New class="org.eclipse.jetty.ajp.Ajp13SocketConnector">
     <Set name="port">8009</Set>
   </New>
 </Arg>
</Call>
```
