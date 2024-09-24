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

import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

class WriteServerConfigTask extends DefaultTask {

  private ScmServerExtension extension

  @Nested
  ScmServerExtension getExtension() {
    return extension
  }

  void setExtension(ScmServerExtension extension) {
    this.extension = extension
  }

  @OutputFile
  File getServerConfig() {
    return new File(project.buildDir, 'server/config.json')
  }

  @TaskAction
  void execute() {
    File warFile = extension.getWarFile()
    if (warFile == null) {
      Configuration configuration = extension.getConfiguration()
      if (configuration == null) {
        throw new GradleException("warFile or configuration must be used")
      }

      def artificat = configuration.resolvedConfiguration
        .resolvedArtifacts
        .find {
          it.extension == 'war'
        }

      if (artificat == null) {
        throw new GradleException("could not find war file in configuration")
      }

      warFile = artificat.getFile()
    }

    File serverConfig = getServerConfig()
    serverConfig.getParentFile().mkdirs()

    def config = [
      port: extension.getPort(),
      contextPath: '/scm',
      stage: 'DEVELOPMENT',
      headerSize: 16384,
      openBrowser: extension.openBrowser,
      warFile: warFile.toString(),
      livereloadUrl: extension.liveReload ? 'http://localhost:3000' : null
    ]
    if (extension.loggingConfiguration != null && extension.loggingConfiguration.exists()) {
      config.loggingConfiguration = extension.loggingConfiguration.toString()
    }
    serverConfig.text = JsonOutput.toJson(config)
  }

}
