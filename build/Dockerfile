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

FROM adoptopenjdk/openjdk11:x86_64-debian-jdk-11.0.9_11.1

ENV DOCKER_VERSION=19.03.8 \
    DOCKER_CHANNEL=stable \
    DOCKER_CHECKSUM=7f4115dc6a3c19c917f8b9664d7b51c904def1c984e082c4600097433323cf6f

# fake modprobe
COPY modprobe.sh /usr/local/bin/modprobe

# install required packages
RUN set -eux; \
 apt-get update \
 && apt-get install --no-install-recommends -y \
    # mercurial is requried for integration tests of the scm-hg-plugin
    mercurial \
    # git is required by yarn install of scm-ui
    git \
    # the following dependencies are required for cypress tests and are copied from
    # https://github.com/cypress-io/cypress-docker-images/blob/master/base/12.18.3/Dockerfile
    libgtk2.0-0 \
    libgtk-3-0 \
    libnotify-dev \
    libgconf-2-4 \
    libgbm-dev \
    libnss3 \
    libxss1 \
    libasound2 \
    libxtst6 \
    xauth \
    xvfb \
 # download docker
 && curl -o docker-${DOCKER_VERSION}.tgz https://download.docker.com/linux/static/${DOCKER_CHANNEL}/x86_64/docker-${DOCKER_VERSION}.tgz \
 && echo "${DOCKER_CHECKSUM} docker-${DOCKER_VERSION}.tgz" > docker-${DOCKER_VERSION}.sha256sum \
 && sha256sum -c docker-${DOCKER_VERSION}.sha256sum \
 # extract docker and install it to /usr/local/bin
 && tar --extract \
    		--file docker-${DOCKER_VERSION}.tgz \
    		--strip-components 1 \
    		--directory /usr/local/bin/ \
 # verify docker installation
 && docker --version \
 # remove temporary files
 && rm -f docker-${DOCKER_VERSION}.tgz docker-${DOCKER_VERSION}.sha256sum \
 # clear apt caching
 && apt-get clean \
 && rm -rf /var/lib/apt/lists/*
