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

import com.google.common.hash.HashCode
import com.google.common.hash.Hashing
import com.google.common.io.Files
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.*

class PackageYamlTask extends DefaultTask {

  private String type
  private File artifact

  @Input
  String getType() {
    return type
  }

  void setType(String type) {
    this.type = type
  }

  @Optional
  @InputFile
  File getArtifact() {
    return artifact
  }

  void setArtifact(File artifact) {
    this.artifact = artifact
  }

  @OutputFile
  File getOutputFile() {
    return new File(project.buildDir, 'libs/package.yml')
  }

  @TaskAction
  void execute() {
    File packageYaml = getOutputFile()
    if (packageYaml.exists() && !packageYaml.delete()) {
      throw new GradleException("failed to remove outdated package.yml");
    }
    if (artifact != null) {
      String repository = project.version.contains('-SNAPSHOT') ? 'snapshots' : 'releases'
      HashCode hashCode = Files.asByteSource(artifact).hash(Hashing.sha256())
      packageYaml << """
        type: ${type}
        checksum: ${hashCode}
        url: https://packages.scm-manager.org/repository/${repository}/sonia/scm/packaging/${type}/${project.version}/${artifact.name}
      """.stripIndent().replaceAll(/^\s+/, '')
    } else {
      packageYaml << "type: ${type}\n"
    }
  }

}
