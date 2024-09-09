---
title: Migrate a v1 plugin
---

Before starting, make sure to read the [Plugin Development](../plugin-development).

To migrate an existing SCM-Manager 1.x Plugin, you have to do the following steps:

### Maven (pom.xml)

* create a separate branch for the new version
* It might be helpful to start and review the old version of the plugin via `mvn scmp:run` for later reference.
* Import .gitignore & .editorconfig from SCMM
* You might run the build once and review and fix SCMMv1 deprecation warnings. SCMMv2 gets rids of all deprecated
  classes.
* update the version of the parent artifact (sonia.scm.plugins:scm-plugins) to the minimum version of SCM-Manager 2 you
  are planning for your plugin
* change the packaging type of your plugin to smp
* remove the sonia.scm.maven:scm-maven-plugin from the pom
* remove servlet-api from the list of dependencies (not always the case)

```diff
diff -r a988f4cfb7ab pom.xml
--- a/pom.xml   Thu Dec 10 20:32:26 2015 +0100
+++ b/pom.xml   Tue Oct 30 11:49:35 2018 +0100
@@ -6,13 +6,14 @@
   <parent>
     <artifactId>scm-plugins</artifactId>
     <groupId>sonia.scm.plugins</groupId>
-    <version>1.15</version>
+    <version>2.0.0-rc7</version>
   </parent>
 
   <groupId>sonia.scm.plugins</groupId>
   <artifactId>scm-mail-plugin</artifactId>
   <version>1.6-SNAPSHOT</version>
   <name>scm-mail-plugin</name>
+  <packaging>smp</packaging>
   <url>https://bitbucket.org/sdorra/scm-mail-plugin</url>
   <description>
     The mail plugin provides an api for sending e-mails. 
@@ -28,13 +29,6 @@
   <dependencies>
 
-    <dependency>
-      <groupId>jakarta.servlet</groupId>
-      <artifactId>servlet-api</artifactId>
-      <version>${servlet.version}</version>
-      <scope>provided</scope>
-    </dependency>
 
     <dependency>
       <groupId>org.codemonkey.simplejavamail</groupId>
       <artifactId>simple-java-mail</artifactId>
       <version>2.4</version>
@@ -52,18 +46,6 @@
     <jakartamail.version>1.4.7</jakartamail.version>
   </properties>
   
-  <build>
-    <plugins>
-      
-      <plugin>
-        <groupId>sonia.scm.maven</groupId>
-        <artifactId>scm-maven-plugin</artifactId>
-        <version>1.22</version>
-      </plugin>
-      
-    </plugins>
-  </build>
   
   <repositories>
     
     <repository>
```

### Plugin Descriptor (src/main/resources/META-INF/scm/plugin.xml)

* add the following dtd to the top of the
  plugin.xml: `<!DOCTYPE plugin SYSTEM "https://download.scm-manager.org/dtd/plugin/2.0.0-01.dtd">`
* add an scm-version element with the value 2 to the plugin.xml
* remove resources and packages from plugin.xml

```diff
diff -r a988f4cfb7ab src/main/resources/META-INF/scm/plugin.xml
--- a/src/main/resources/META-INF/scm/plugin.xml        Thu Dec 10 20:32:26 2015 +0100
+++ b/src/main/resources/META-INF/scm/plugin.xml        Tue Oct 30 11:55:15 2018 +0100
@@ -1,4 +1,5 @@
 <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
+<!DOCTYPE plugin SYSTEM "https://download.scm-manager.org/dtd/plugin/2.0.0-01.dtd">
 <!--
 
     Copyright (c) 2010, Sebastian Sdorra
@@ -34,6 +35,8 @@
 
 <plugin>
 
+  <scm-version>2</scm-version>
+
   <information>
     <author>Sebastian Sdorra</author>
     <wiki>https://bitbucket.org/sdorra/scm-manager/wiki/mail-plugin</wiki>
@@ -44,12 +47,4 @@
     <min-version>${project.parent.version}</min-version>
   </conditions>
 
-  <packages>
-    <package>sonia.scm.mail</package>
-  </packages>
-
-  <resources>
-    <script>/sonia/scm/mail/sonia.mail.js</script>
-  </resources>

+  <resources>
+    <script>assets/scm-mail-plugin.bundle.js</script>
+  </resources>

 </plugin>
```

### Java sources (src/main/java)

* try to compile the sources: `mvn compile`
* fix problems (TODO more help here)
* Remove XML accept headers from REST Resource classes -> SCMMv2 supports JSON only
* Migrate REST Resources (e.g. `v2`, add to Index Resource, Update Links) - See core plugins Git, Hg, Svn,
  e.g. [`GitConfigResource`](https://github.com/scm-manager/scm-manager/blob/develop/scm-plugins/scm-git-plugin/src/main/java/sonia/scm/api/v2/resources/GitConfigResource.java)

### UI (src/main/js, src/main/webapp)

* remove all SCM-Manager 1.x ui code from resource directory (src/main/resources)
* create `package.json` with the following content (replace name-of-plugin with the name of your plugin):

```json
{
  "name": "@scm-manager/name-of-plugin",
  "license": "AGPLv3",
  "main": "src/main/js/index.tsx",
  "scripts": {
    "build": "ui-scripts plugin",
    "watch": "ui-scripts plugin-watch",
    "postinstall": "ui-plugins postinstall"
  },
  "dependencies": {
    "@scm-manager/ui-plugins": "2.0.0"
  }
}
```

* create a `tsconfig.json` with the following content:

```json
{
  "extends": "@scm-manager/tsconfig",
  "include": [
    "./src/main/js"
  ]
}
```

* run `mvn process-resources` to install the required JavaScript libraries
* create new ui at `src/main/js` (for JavaScript code) and `src/main/webapp` (for static files) (TODO more help)
* Start SCM-Manager with the plugin using `mvn run` - features hot reloading of UI & Java Code.  
  In order for Java classpath resources to be reloaded in IntelliJ, pressing compile is necessary.

Some more hints:

* For Configuration UIs
  use [`ConfigurationBinder`](https://github.com/scm-manager/scm-manager/blob/develop/scm-ui/ui-components/src/config/ConfigurationBinder.tsx)
  - See core plugins Git, Hg, Svn,
  e.g. [scm-git-plugin/index.ts](https://github.com/scm-manager/scm-manager/blob/develop/scm-plugins/scm-git-plugin/src/main/js/index.ts)
  .  
  Note that `readOnly` property checks if update link is returned by REST resource
* Don't forget [i18n for Plugins](../i18n-for-plugins)

# Further reading

* [UI Extensions](../../ui-extensions) - Extend the SCM-Manager UI
* [scm-manager/ui-components](https://github.com/scm-manager/scm-manager/tree/develop/scm-ui/ui-components) - Reusable
  UI components within SCM-Manager
* [smp-maven-plugin](https://github.com/scm-manager/smp-maven-plugin) - Plugin that facilitates efficient plugin
  development for SCMM
