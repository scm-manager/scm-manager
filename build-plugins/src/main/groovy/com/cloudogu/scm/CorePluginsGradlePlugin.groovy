/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */


package com.cloudogu.scm


import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

class CorePluginsGradlePlugin implements Plugin<Project> {

  void apply(Project project) {
    Configuration corePlugin = project.configurations.create("corePlugin")
    corePlugin.canBeConsumed = false
    corePlugin.canBeResolved = true

    project.tasks.register("copy-core-plugins", CopyCorePluginsTask) {
      it.configuration = corePlugin
      it.targetDirectory = project.layout.buildDirectory.dir("war/WEB-INF/plugins")
    }
  }

}
