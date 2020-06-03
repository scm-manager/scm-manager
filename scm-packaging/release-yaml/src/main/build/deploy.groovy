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

import org.eclipse.jgit.api.Git
import org.apache.maven.settings.crypto.DefaultSettingsDecryptionRequest
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider

def getCredentials() {
  def decrypter = session.lookup("org.apache.maven.settings.crypto.SettingsDecrypter")
  def server = session.settings.getServer("github.com/scm-manager/website")
  def result = decrypter.decrypt(new DefaultSettingsDecryptionRequest(server))
  return result.server
}

if (project.version.contains("-SNAPSHOT")) {
  println "we do not deploy snapshot releases"
  return
}

File websiteDir = new File(project.build.directory, "website")
websiteDir.deleteDir()

Git git = Git.cloneRepository()
  .setURI("https://github.com/scm-manager/website.git")
  .setDirectory(websiteDir)
  .setBranch("refs/heads/master")
  .call()

String targetPath = "content/releases/" + project.version.replace('.', "-") + ".yml"

File source = new File(project.build.directory, "release.yml")
File target = new File(websiteDir, targetPath)
target << source.text

git.add().addFilepattern(targetPath).call()
git.commit()
  .setAuthor("CES Marvin", "cesmarvin@cloudogu.com")
  .setMessage("add release descriptor for ${project.version}")
  .call()

def credentials = getCredentials()
git.push()
  .setCredentialsProvider(new UsernamePasswordCredentialsProvider(credentials.username, credentials.password))
  .call()
