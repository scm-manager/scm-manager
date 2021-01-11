package com.cloudogu.scm


import com.google.common.io.Files
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class PrepareHomeTask extends DefaultTask {

  @Nested
  ScmServerExtension extension

  @OutputDirectory
  File getPluginFolder() {
    return new File(extension.getHome(), "plugins")
  }

  @TaskAction
  void prepareHome() {
    File pluginFolder = getPluginFolder()
    if (!pluginFolder.exists() && !pluginFolder.mkdirs()) {
      throw new GradleException("failed to create plugin folder at ${pluginFolder}")
    }

    Configuration configuration = extension.getPlugins()
    if (configuration != null) {
      List<File> plugins = configuration.resolvedConfiguration
        .resolvedArtifacts
        .collect { artifact ->
          if (artifact.extension == 'smp') {
            return artifact.file
          }
        }.findAll { file ->
          file != null && file.exists()
        }
      plugins.forEach { source ->
        File target = new File(pluginFolder, source.getName())
        Files.copy(source, target)
      }
    }
  }

}
