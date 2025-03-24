---
title: Building SCM-Manager from Source
---

### Software Requirements

- JDK 1.8 or higher
    ([download](https://openjdk.java.net/install/))
- Maven 3 or higher ([download](https://maven.apache.org/))
- Git ([download](https://git-scm.com/))

### Build SCM-Manager from Source

```bash
git clone https://github.com/scm-manager/scm-manager.git
cd scm-manager
git checkout develop
./gradlew build
```

After gradle finished, the war bundle is located at
**scm-webapp/build/scm-webapp.war** and the standalone version is
located at **scm-server/build/scm-server-app**.

You can also start a dev server using `./gradlew run`. SCM-Manager is served at <http://localhost:8081/scm>.
