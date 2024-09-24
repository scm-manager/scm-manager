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

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty

class BuildInfoTask extends DefaultTask {

  @Input
  final Property<String> version = project.objects.property(String)

  @OutputDirectory
  final DirectoryProperty targetDirectory = project.objects.directoryProperty()

  @TaskAction
  void exec() {
    File directory = new File(targetDirectory.get().asFile, 'META-INF/scm')
    if (!directory.exists() && !directory.mkdirs()) {
      throw new GradleException("failed to create build info directory: ${directory}")
    }

    File file = new File(directory, 'build-info.properties')
    file.text = "version = ${version.get()}"
  }

}
