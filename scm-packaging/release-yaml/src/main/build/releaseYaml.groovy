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

import groovy.io.FileType
import org.yaml.snakeyaml.Yaml
import java.text.SimpleDateFormat

Yaml yaml = new Yaml();

def release = [:]
release.tag = project.version
SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
sdf.setTimeZone(TimeZone.getTimeZone("UTC"))
release.date = sdf.format(new Date())
release.packages = []

def moduleDirectory = project.basedir.getParentFile()
moduleDirectory.eachFile FileType.DIRECTORIES, {
  def packageFile = new File(it, "target/package.yml")
  if (packageFile.exists()) {
    packageFile.withReader { r ->
      def pkg = yaml.load(r)
      release.packages.add(pkg)
    }
  }
}

def buildir = new File(project.build.directory)
if (!buildir.exists()) {
  buildir.mkdirs()
}

def file = new File(buildir, "release.yml")
file << yaml.dump(release)
