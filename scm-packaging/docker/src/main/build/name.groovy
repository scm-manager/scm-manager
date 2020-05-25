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

import java.text.SimpleDateFormat

def repository = "docker.io/scmmanager/scm-manager"
def version = project.version

if (version.contains("SNAPSHOT")) {
  repository = "docker.io/cloudogu/scm-manager"

  def commitHash = System.getenv("GIT_COMMIT")
  def buildNumber = System.getenv("BUILD_NUMBER")

  def snapshotVersion
  if (commitHash != null && buildNumber != null) {
    snapshotVersion = "${commitHash.substring(0,7)}-${buildNumber}"
  } else {
    def format = new SimpleDateFormat("yyyyMMdd-HHmmss")
    snapshotVersion = format.format(new Date())
  }

  version = version.replace("SNAPSHOT", snapshotVersion)
}

project.properties.setProperty("docker.repository", repository)
project.properties.setProperty("docker.tag", version)
