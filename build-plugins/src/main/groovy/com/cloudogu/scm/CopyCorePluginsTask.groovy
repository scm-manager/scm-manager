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

import com.google.common.hash.Hashing
import com.google.common.io.Files
import groovy.xml.MarkupBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class CopyCorePluginsTask extends DefaultTask {

  @Classpath
  final Property<Configuration> configuration = project.objects.property(Configuration)

  @OutputDirectory
  final DirectoryProperty targetDirectory = project.objects.directoryProperty()

  @Internal
  final Provider<List<File>> lookup = configuration.map {
    it.resolvedConfiguration
      .resolvedArtifacts
      .findAll {
        it.extension == "smp"
      }
      .collect {
        it.file
      }
  }

  @TaskAction
  void execute() {
    File directory = targetDirectory.get().asFile
    if (!directory.exists() && !directory.mkdirs()) {
      throw new GradleException("failed to create plugin directory: ${directory}")
    }

    List<File> plugins = lookup.get()

    project.sync {
      into(directory)
      from(plugins)
    }

    writeReadme(directory)
    writeIndex(directory, plugins)
  }

  private void writeReadme(File directory) {
    File readme = new File(directory, 'README')
    readme.text = 'Directory for SCM-Manager core plugin\n'
  }

  private void writeIndex(File directory, List<File> pluginFiles) {
    File file = new File(directory, "plugin-index.xml")
    file.withWriter { writer ->
      def xml = new MarkupBuilder(writer)
      xml.'plugin-index' {
        pluginFiles.forEach { pluginFile ->
          plugins {
            name pluginFile.name
            checksum Files.asByteSource(pluginFile).hash(Hashing.sha256())
          }
        }
      }
    }
  }

}
