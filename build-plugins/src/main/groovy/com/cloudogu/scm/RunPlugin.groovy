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
import org.gradle.api.artifacts.VersionCatalogsExtension
import com.github.gradle.node.NodeExtension

class RunPlugin implements Plugin<Project> {

  void apply(Project project) {
    def extension = project.extensions.create("scmServer", ScmServerExtension, project)

    project.plugins.apply("com.github.node-gradle.node")
    def nodeExt = project.extensions.getByType(NodeExtension)
    def libs = project.extensions.getByType(VersionCatalogsExtension).named('libs')
    nodeExt.download.set(true)
    nodeExt.version.set(libs.findVersion('node').get().requiredVersion)
    nodeExt.yarnVersion.set('1.22.22')
    nodeExt.nodeProjectDir.set(project.rootProject.projectDir)

    project.tasks.getByName('yarn').configure {
      dependsOn ':scm-ui:yarn'
      inputs.file( project.rootProject.file('yarn.lock') )
      outputs.dir( project.rootProject.file('node_modules') )

      description = "Install ui dependencies"
    }

    project.tasks.register('write-server-config', WriteServerConfigTask) {
      it.extension = extension
      dependsOn 'dev-war'
    }
    project.tasks.register('prepare-home', PrepareHomeTask) {
      it.extension = extension
      dependsOn 'dev-war'
    }
    project.tasks.register("run", RunTask) {
      it.extension = extension
      it.nodeExtension = nodeExt
      dependsOn 'write-server-config', 'prepare-home', 'yarnSetup'
    }
  }

}
