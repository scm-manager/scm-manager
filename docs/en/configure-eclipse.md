---
title: Configuring Eclipse
---

There are multiple approaches to using Maven projects within Eclipse. If
you use an approach that isn\'t listed here, feel free to add it.

## Use Maven to generate Eclipse project files

1.  First, [build scm-manager from source](build-from-source.md).
2.  Open a shell to the root of your scm-manager source.
3.  Run the following command:\
    `mvn eclipse:configure-workspace -Declipse.workspace=YOUR_WORKSPACE_PATH`
4.  Run the following command:\
    `mvn eclipse:eclipse -DdownloadSources=true -DdownloadJavadocs=true`
5.  Open Eclipse using the specified workspace.
6.  In Eclipse, select the \"File\", \"Import\...\" menu.
7.  Select the \"General\", \"Existing Projects into Workspace\" item
    and click \"Next\".
8.  Click \"Browse\" and select the root of your scm-manager source.
9.  All projects will be selected by default. Click \"Finish\".

### Troubleshooting

You may encounter an error during the \"eclipse:eclipse\" step that
looks like this:
```
[INFO] Request to merge when 'filtering' is not identical. Original=resource src
/main/resources: output=target/classes, include=[META-INF/scm/plugin.xml], exclu
de=[**/*.java], test=false, filtering=true, merging with=resource src/main/resou
rces: output=target/classes, include=[], exclude=[META-INF/scm/plugin.xml|**/*.j
ava], test=false, filtering=false
```

This error is caused by a bug in version 2.7 and 2.8 of the Maven
eclipse plugin. See below for one workaround. Other discussion of this
issue can be found
[here](http://forums.atlassian.com/thread.jspa?threadID=34952&tstart=1).

1. Add the following line within the <settings> element of your /.m2/settings.xml file:\
    `<usePluginRegistry>true</usePluginRegistry>`

1. Add the file /.m2/plugin-registry.xml with the following contents:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<pluginRegistry
  xsi:schemaLocation="http://maven.apache.org/PLUGIN_REGISTRY/1.0.0 http://maven.apache.org/xsd/plugin-registry-1.0.0.xsd"
  xmlns="http://maven.apache.org/PLUGIN_REGISTRY/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-eclipse-plugin</artifactId>
      <useVersion>2.6</useVersion>
      <rejectedVersions>
        <rejectedVersion>2.7</rejectedVersion>
        <rejectedVersion>2.8</rejectedVersion>
      </rejectedVersions>
    </plugin>
  </plugins>
</pluginRegistry>
```

If you have .project files in any parent directory, the Eclipse import
step will not search beneath that parent for any further projects.
Delete the .project file in the parent and try the import again.

If you get errors in Eclipse about not finding M2\_REPO, it means that
the eclipse:configure-workspace step didn't work. One cause for this
could be using \"\~\" or other special characters in your workspace path
that the plugin doesn't resolve properly. Either re-run
eclipse:configure-workspace, or manually create a classpath variable
named M2\_REPO pointing to the root of your local maven repository
directory.

If you get compilation errors on JAXB or ServiceLoader, your Eclipse may
not be configured to use Java 6.
