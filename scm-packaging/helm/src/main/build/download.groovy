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

private String getArchitecture() {
  String architecture = System.getProperty("os.arch").toLowerCase(Locale.ENGLISH)

  if (architecture.equals("x86_64") || architecture.equals("amd64")) {
    return "amd64"
  } else if (architecture.equals("x86") || architecture.equals("i386")) {
    return "386"
  } else if (architecture.contains("arm64")) {
    return "arm64"
  } else if (architecture.equals("aarch32") || architecture.startsWith("arm")) {
    return "arm"
  } else if (architecture.contains("ppc64le") || (architecture.contains("ppc64") && System.getProperty("sun.cpu.endian").equals("little"))) {
    return "ppc64le"
  }

  throw new IllegalStateException("Unsupported architecture: ${architecture}")
}

private String getExtension() {
  String osName = System.getProperty("os.name").toLowerCase(Locale.ENGLISH)
  if (osName.startsWith("windows")) {
    return ".zip"
  }
  return "tar.gz"
}

private String getOperatingSystem() {
  String osName = System.getProperty("os.name").toLowerCase(Locale.ENGLISH)
  if (osName.startsWith("linux")) {
    return "linux"
  } else if (osName.startsWith("mac") || osName.startsWith("darwin")) {
    return "darwin"
  } else if (osName.startsWith("windows")) {
    return "windows"
  }
  throw new IllegalStateException("Unsupported operating system: ${osName}")
}

project.properties.setProperty("helm.os", getOperatingSystem())
project.properties.setProperty("helm.arch", getArchitecture())
project.properties.setProperty("helm.ext", getExtension())
