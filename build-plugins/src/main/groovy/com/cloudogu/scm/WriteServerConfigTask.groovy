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

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal

import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import groovy.xml.MarkupBuilder
import java.io.BufferedWriter
import org.gradle.api.tasks.Input

import com.google.common.hash.Hashing
import com.google.common.io.Files
import groovy.json.JsonOutput

class WriteServerConfigTask extends DefaultTask {

  @Input
  public String getHome() {
    if (project.hasProperty('home')) {
      return project.getProperty('home')
    }
    return new File(project.buildDir, 'scm-home').toString()
  }

  @Input
  public int getPort() {
    if (project.hasProperty('port')) {
      return Integer.parseInt(project.getProperty('port'))
    }
    return 8081
  }

  @InputFile
  public File getWarFile() {
    return new File(project.buildDir, 'libs/scm-webapp-dev.war')
  }

  @OutputFile
  public File getServerConfig() {
    return new File(project.buildDir, 'server/config.json')
  }

  @TaskAction
  void execute() {
    File serverConfig = getServerConfig()
    serverConfig.getParentFile().mkdirs()
    serverConfig.text = JsonOutput.toJson([
      home: getHome(), 
      port: getPort(), 
      contextPath: '/scm',
      stage: 'DEVELOPMENT',
      headerSize: 16384,
      openBrowser: true,
      warFile: getWarFile().toString()
    ])
  }

}
