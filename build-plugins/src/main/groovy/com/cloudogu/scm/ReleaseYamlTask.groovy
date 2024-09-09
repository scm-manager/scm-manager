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
import org.gradle.api.GradleException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.yaml.snakeyaml.Yaml

import java.text.SimpleDateFormat

class ReleaseYamlTask extends DefaultTask {

  private Configuration configuration

  @Classpath
  Configuration getConfiguration() {
    return configuration
  }

  void setConfiguration(Configuration configuration) {
    this.configuration = configuration
  }

  @OutputFile
  File getOutputFile() {
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
