/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the 'Software'), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
