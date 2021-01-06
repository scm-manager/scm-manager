/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */


package com.cloudogu.scm


import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Input
import org.gradle.api.Project

class ScmServerExtension implements Serializable {

  private Project project
  private Configuration configuration
  private boolean openBrowser = true
  private boolean liveReload = true
  private File warFile
  private File loggingConfiguration

  ScmServerExtension(Project project) {
    this.project = project
  }

  @Input
  boolean isOpenBrowser() {
    return openBrowser
  }

  void setOpenBrowser(boolean openBrowser) {
    this.openBrowser = openBrowser
  }

  @Input
  boolean isLiveReload() {
    return liveReload
  }

  void setLiveReload(boolean liveReload) {
    this.liveReload = liveReload
  }

  @Input
  String getHome() {
    if (project.hasProperty('home')) {
      return project.getProperty('home')
    }
    return new File(project.buildDir, 'scm-home').toString()
  }

  @Input
  int getPort() {
    if (project.hasProperty('port')) {
      return Integer.parseInt(project.getProperty('port'))
    }
    return 8081
  }

  @Optional
  @InputFile
  File getWarFile() {
    return warFile
  }

  void setWarFile(File warFile) {
    this.warFile = warFile
  }

  @Optional
  @Classpath
  Configuration getConfiguration() {
    return configuration
  }

  void setConfiguration(Configuration configuration) {
    this.configuration = configuration
  }

  void configuration(String configuration) {
    setConfiguration(project.configurations.getByName(configuration))
  }

  @Optional
  @InputFile
  File getLoggingConfiguration() {
    return loggingConfiguration
  }

  void setLoggingConfiguration(File loggingConfiguration) {
    this.loggingConfiguration = loggingConfiguration
  }
}
