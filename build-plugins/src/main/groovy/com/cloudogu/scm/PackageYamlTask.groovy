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
