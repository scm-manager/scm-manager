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

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Internal

import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import groovy.xml.MarkupBuilder
import java.io.BufferedWriter

import com.google.common.hash.Hashing
import com.google.common.io.Files

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
