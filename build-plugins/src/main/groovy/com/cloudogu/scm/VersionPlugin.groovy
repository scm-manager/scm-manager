package com.cloudogu.scm

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.util.VersionNumber

import java.nio.charset.StandardCharsets

class VersionTasks {

  static void configure(Project project) {
    project.tasks.register("setVersion", SetVersionTask) {
      it.group = "help"
      it.description = "Set version for the plugin e.g.: setVersion --newVersion=x.y.z)"
    }
    project.tasks.register("setVersionToNextSnapshot", SetVersionToNextSnapshot) {
      it.group = "help"
      it.description = "Set version of the plugin to the next snapshot version"
    }
  }

  private static void setVersion(Project project, String newVersion) {
    File propertiesFile = new File(project.rootDir, 'gradle.properties')

    if (project.version == newVersion) {
      println "project uses already version ${newVersion}"
      return
    }

    // UTF-8 is used since java 9, java 8 uses ISO-8859-1
    // TODO do we have to implement something to support java 9
    def lines = propertiesFile.readLines(StandardCharsets.UTF_8.toString())
    def newLines = lines.collect{ line ->
      if (line.trim().startsWith("version")) {
        return "version = ${newVersion}"
      }
      return line
    }

    println "set version from ${project.version} to ${newVersion}"
    propertiesFile.withWriter(StandardCharsets.UTF_8.toString()) {writer ->
      newLines.forEach { line ->
        writer.writeLine(line)
      }
    }
  }

  static class SetVersionTask extends DefaultTask {

    @Input
    @Option(option = "newVersion", description = "Sets new version for project")
    String newVersion

    @TaskAction
    void execute() {
      if (!newVersion?.trim()) {
        throw new GradleException("newVersion option is required e.g.: --newVersion=x.y.z")
      }
      setVersion(project, newVersion)
    }
  }

  static class SetVersionToNextSnapshot extends DefaultTask {

    @TaskAction
    void execute() {
      VersionNumber v = VersionNumber.parse(project.version)
      String version = "${v.major}.${v.minor}.${v.micro + 1}-SNAPSHOT"
      setVersion(project, version)
    }
  }

}
