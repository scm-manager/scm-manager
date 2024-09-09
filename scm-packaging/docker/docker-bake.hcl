#
# Copyright (c) 2020 - present Cloudogu GmbH
#
# This program is free software: you can redistribute it and/or modify it under
# the terms of the GNU Affero General Public License as published by the Free
# Software Foundation, version 3.
#
# This program is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
# details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program. If not, see https://www.gnu.org/licenses/.
#

group "prod" {
  targets = [
    "alpine",
    "debian"
  ]
}

variable "VERSION" {
  default = "unknown"
}

variable "COMMIT_SHA" {
  default = "unknown"
}

variable "IMAGE" {
  default = "docker.io/cloudogu/scm-manager"
}

variable "IS_HOTFIX" {
  default = false
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
    "org.opencontainers.image.licenses" = "AGPLv3"
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
    IS_HOTFIX ? "" : "${IMAGE}:latest",
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
  platforms = ["linux/amd64", "linux/arm64/v8"]
}
