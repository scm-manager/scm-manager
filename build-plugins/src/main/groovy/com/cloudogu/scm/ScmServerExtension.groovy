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
  private Configuration plugins

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
    if (project.hasProperty('scm.home')) {
      return project.getProperty('scm.home')
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

  @Optional
  @Classpath
  Configuration getPlugins() {
    return plugins
  }

  void setPlugins(Configuration plugins) {
    this.plugins = plugins
  }
}
