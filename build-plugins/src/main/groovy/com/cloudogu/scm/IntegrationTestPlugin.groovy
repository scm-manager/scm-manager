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
import com.moowork.gradle.node.NodeExtension

class IntegrationTestPlugin implements Plugin<Project> {

  void apply(Project project) {
    project.plugins.apply("com.github.node-gradle.node")

    def extension = project.extensions.create("scmServer", ScmServerExtension, project)

    project.tasks.register('write-server-config', WriteServerConfigTask) {
      it.extension = extension
    }

    project.tasks.register('prepare-home', PrepareHomeTask) {
      it.extension = extension
    }

    def nodeExt = NodeExtension.get(project)

    project.tasks.register("startScmServer", RunTask) {
      it.extension = extension
      it.nodeExtension = nodeExt
      it.waitForCompletion = false
      it.frontend = false
      it.configFileDirectory = './src/main/resources'
      dependsOn 'write-server-config', 'prepare-home'
    }

    project.tasks.register("stopScmServer", StopScmServer) {
      it.extension = extension
    }
  }

}
