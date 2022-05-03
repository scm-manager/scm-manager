#
# MIT License
#
# Copyright (c) 2020-present Cloudogu GmbH and Contributors
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.
#

group "prod" {
  targets = [
    "alpine",
    "debian"
  ]
}

variable "VERSION" {
  default = "2.32.2"
}

variable "COMMIT_SHA" {
  default = "unknown"
}

variable "IMAGE" {
  default = "docker.io/cloudogu/scm-manager"
}

target "base" {
  context = "."
  args = {
    VERSION = VERSION
    COMMIT_SHA = COMMIT_SHA
  }
  labels = {
    "org.opencontainers.image.vendor" = "Cloudogu GmbH"
    "org.opencontainers.image.title" = "Official SCM-Manager image"
    "org.opencontainers.image.description" = "The easiest way to share and manage your Git, Mercurial and Subversion repositories"
    "org.opencontainers.image.url" = "https://scm-manager.org/"
    "org.opencontainers.image.source" = "https://github.com/scm-manager/docker"
    "org.opencontainers.image.licenses" = "MIT"
    "org.opencontainers.image.version" = VERSION
    "org.opencontainers.image.revision" = COMMIT_SHA
  }
}

target "dev" {
  inherits = ["base"]
  dockerfile = "Dockerfile.alpine"
  tags = ["${IMAGE}:${VERSION}"]  
}

target "alpine" {
  inherits = ["base"]
  dockerfile = "Dockerfile.alpine"
  tags = [
    "${IMAGE}:latest",
    "${IMAGE}:${VERSION}",
    "${IMAGE}:${VERSION}-alpine"
  ]
  platforms = ["linux/amd64", "linux/arm64/v8"]
}

target "debian" {
  inherits = ["base"]
  dockerfile = "Dockerfile.debian"
  tags = [
    "${IMAGE}:${VERSION}-debian"
  ]
  platforms = ["linux/amd64", "linux/arm64/v8", "linux/arm/v7"]
}
