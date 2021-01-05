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
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Internal
import org.gradle.api.GradleException

import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import groovy.xml.MarkupBuilder
import java.io.BufferedWriter
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile

import com.google.common.hash.Hashing
import com.google.common.hash.HashCode
import com.google.common.io.Files
import groovy.json.JsonOutput

import java.text.SimpleDateFormat
import org.yaml.snakeyaml.Yaml


class ReleaseYamlTask extends DefaultTask {

  private Configuration configuration

  @Classpath
  public Configuration getConfiguration() {
    return configuration
  }

  public void setConfiguration(Configuration configuration) {
    this.configuration = configuration
  }

  @OutputFile
  public File getOutputFile() {
    return new File(project.buildDir, 'libs/release.yml')
  }

  @TaskAction
  void execute() {
    Yaml yaml = new Yaml();
    def release = [:]
    release.tag = project.version
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    sdf.setTimeZone(TimeZone.getTimeZone("UTC"))
    release.date = sdf.format(new Date())
    release.packages = []

    def files = configuration.getResolvedConfiguration().getResolvedArtifacts().collect { artifact ->
      File file = artifact.file
      if (file.exists() && (file.name.endsWith(".yml")) || file.name.endsWith(".yaml")) {
        return file
      }
    }

    files.forEach { file ->
      file.withReader { r -> 
        def pkg = yaml.load(r)
        release.packages.add(pkg)
      }
    }

    File target = getOutputFile()
    File directory = target.getParentFile()
    if (!directory.exists() && !directory.mkdirs()) {
      throw new GradleException("failed to create directory " + directory);
    }

    if (target.exists() && !target.delete()) {
      throw new GradleException("failed to delete outdated release.yml " + target);
    }

    target << yaml.dump(release)
  }

}
