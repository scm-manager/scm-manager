package com.cloudogu.scm

import org.gradle.api.Plugin
import org.gradle.api.Project

class CIPlugin implements Plugin<Project> {
  @Override
  void apply(Project project) {
    project.ext.TouchFiles = TouchFilesTask
  }
}
