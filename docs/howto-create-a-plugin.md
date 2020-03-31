# How to create your own plugin

### Software Requirements

-   JDK 1.7 or higher
    ([download](http://www.oracle.com/technetwork/java/index.html))
-   Maven 3 or higher ([download](http://maven.apache.org/))

### Create plugin

```bash
mvn archetype:generate\
   -DarchetypeGroupId=sonia.scm.maven\
   -DarchetypeArtifactId=scm-plugin-archetype\
   -DarchetypeVersion=1.60\
   -DarchetypeRepository=http://maven.scm-manager.org/nexus/content/groups/public/
```
### Test the plugin

```bash
mvn scmp:run
```

### Samples

- [Hello World](https://bitbucket.org/sdorra/scm-manager/src/1.x/scm-samples/scm-sample-hello/)
- [Authentication Plugin](https://bitbucket.org/sdorra/scm-manager/src/1.x/scm-samples/scm-sample-auth/)

### Further reading

- [Injection framework - Google Guice](http://code.google.com/p/google-guice/)
- [Restful WebService - Jersey](http://jersey.java.net/nonav/documentation/latest/user-guide.html)
- [ XML transformation - JAXB](http://jaxb.java.net/guide/)
- [User interface - Ext JS](http://www.sencha.com/products/extjs3/)

### Questions/Help

If you have questions or you need help, please write to the mailing
list: <https://groups.google.com/forum/#!forum/scmmanager>
