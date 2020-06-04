/**
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

import com.google.common.hash.*
import com.google.common.io.Files

def packageFile = project.attachedArtifacts.find(it -> it.file.name.endsWith("tar.gz")).file
def repository = project.version.contains("-SNAPSHOT") ? "snapshots" : "releases"

HashCode hashCode = Files.asByteSource(packageFile).hash(Hashing.sha256())
def deploymentTarget = project.properties.getProperty("deployment.target")

def file = new File(project.build.directory, "package.yml")
file << """
type: unix
checksum: ${hashCode}
url: ${deploymentTarget}/repository/${repository}/sonia/scm/packaging/unix/${project.version}/unix-${project.version}-app.tar.gz
"""
