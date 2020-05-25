---
title: Plugin Descriptor
---

The plugin descriptor contains informations and instructions for the
scm-manager to integrate the plugin. The descriptor is located at
META-INF/scm/plugin.xml in the package of a plugin.

| Element  | Description  | Multiple  | 
| --- | --- | --- |
| plugin | Root element of the plugin descriptor | | 
| plugin/condition | Plugin condifitions | | 
| plugin/condition/arch | Processor architecture (x86/amd64) | | 
| plugin/condition/min-version | Minimum version of SCM-Manager | | 
| plugin/condition/os | Operation System | | 
| plugin/condition/os/name | Name of the Operating System | X | 
| plugin/information | Contains informations of the plugin for the plugin backend | | 
| plugin/information/artifactId | Maven artifact id | | 
| plugin/information/author | The Author of the plugin | |
| plugin/information/category | Category of the plugin | | 
| plugin/information/description | Description of the plugin | | 
| plugin/information/groupId | Maven group id | | 
| plugin/information/name | Name of the plugin | | 
| plugin/information/screenshots | Contains screenshots of the plugin | | 
| plugin/information/screenshots/screenshot | Single screenshot of the plugin | X | 
| plugin/information/url | The url of the plugin homepage | | 
| plugin/information/version | The current version of the plugin | | 
| plugin/information/wiki | The url of a wiki page | | 
| plugin/packages | Java packages which are being searched for extensions | | 
| plugin/packages/package | Single Java packages which is being searched for extensions | X | 
| plugin/resources | Contains resources for the web interface (stylesheets and JavaScript files) | | 
| plugin/resources/script | JavaScript file for the web interface | X | 
| plugin/resources/stylesheet | Stylesheet for the web interface | X |

Example of the plugin descriptor:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<plugin>

  <!-- 
    Informations for the plugin backend.
    The elements groupId, artifactId, name, version and url
    are automatically added from the pom.xml by a maven plugin (since 1.5).
  -->
  <information>
    <author>Sebastian Sdorra</author>
  </information>

  <!-- 
    pluigin requires SCM-Manager version 1.7 
  -->
  <conditions>
    <min-version>1.7</min-version>
  </conditions>
  
  <!-- 
    register package for plugin extension finder 
   -->
  <packages>
    <package>sonia.scm.jenkins</package>
  </packages>

  <!-- 
    register javascript file 
   -->
  <resources>
    <script>/sonia/scm/sonia.jenkins.js</script>
  </resources>

</plugin>
```
