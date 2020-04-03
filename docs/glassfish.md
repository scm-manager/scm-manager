# SCM-Manager with GlassFish

To use SCM-Manager 1.6 and above with GlassFish 3.x you have to add a
JVM-Option to the GlassFish configuration. Please follow the steps
below.

Open the GlassFish Admin-Console (http://yourserver:4848), login as
admin user, goto Configuration-\>JVM Settings, switch to the JVM Options
tab and add the following JVM-Option:

```bash
-Dcom.sun.enterprise.overrideablejavaxpackages=javax.ws.rs,javax.ws.rs.core,javax.ws.rs.ext
```

Restart the GlassFish-Server.

Source:
<http://jersey.java.net/nonav/documentation/latest/glassfish.html>
