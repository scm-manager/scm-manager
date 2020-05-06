---
title: Building SCM-Manager from source
---

### Software Requirements

- JDK 1.8 or higher
    ([download](https://openjdk.java.net/install/))
- Maven 3 or higher ([download](http://maven.apache.org/))
- Mercurial ([download](https://www.mercurial-scm.org/))

### Build SCM-Manager 2.x from source

```bash
hg clone https://github.com/scm-manager/scm-manager.git
cd scm-manager
git checkout develop
mvn clean install
```

**Note**: if you use the \"package\" phase rather than the \"install\" phase, 
the standalone version may include an old version of the WAR file in the distribution bundle, 
rather than the version you just built.

After mvn finished, the war bundle is located at
**scm-webapp/target/scm-webapp.war** and the standalone version is
located at **scm-server/target/scm-server-app**.

You can also start a dev server using `mvn jetty:run-war -f
scm-webapp`. SCM-Manager is served at <http://localhost:8081/scm>.
