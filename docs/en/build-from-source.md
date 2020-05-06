# Building SCM-Manager from source

### Software Requirements

- JDK 1.7 or higher
    ([download](http://www.oracle.com/technetwork/java/index.html))
- Maven 3 or higher ([download](http://maven.apache.org/))
- Mercurial ([download](https://www.mercurial-scm.org/))

### Build SCM-Manager 1.x from source

```bash
hg clone https://bitbucket.org/sdorra/scm-manager
cd scm-manager
hg update 1.x
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

### REST

Docs:

- Create the documentation: `mvn -f scm-webapp compile -P doc`
- The documentation can be found at scm-webapp/target/restdocs

Note that if using jetty (see above) you have to access
<http://localhost:8081/scm> once, to trigger creation of the
`scmadmin` user. Then you can access the REST api directly
<http://localhost:8081/scm/api/rest>
