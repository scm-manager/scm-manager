#!/bin/bash -v
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

set -euo pipefail
IFS=$'\n\t'

BUILDER=scm-builder
if ! docker buildx inspect ${BUILDER} > /dev/null 2>&1; then
  # https://github.com/docker/buildx/issues/495#issuecomment-761562905
  # https://github.com/docker/buildx#building-multi-platform-images
  # https://hub.docker.com/r/tonistiigi/binfmt
  docker run --privileged --rm tonistiigi/binfmt --install "arm,arm64"
  docker buildx create --name ${BUILDER} --driver docker-container --platform linux/arm/v7,linux/arm64/v8,linux/amd64
  docker buildx inspect --bootstrap ${BUILDER}
else
  echo "builder ${BUILDER} is already installed"
fi

# build and push
docker buildx bake --builder ${BUILDER} -f docker-bake.hcl $@
