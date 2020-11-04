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

FROM adoptopenjdk/openjdk11:jdk-11.0.9_11.1-alpine-slim
ENV SCM_HOME=/var/lib/scm
ENV CACHE_DIR=/var/cache/scm/work

COPY . /

RUN set -x \
 && apk add --no-cache mercurial bash ca-certificates \
 && addgroup -S -g 1000 scm \
 && adduser -S -s /bin/false -G scm -h ${SCM_HOME} -D -H -u 1000 scm \
 && mkdir -p ${SCM_HOME} ${CACHE_DIR} \
 && chmod +x /opt/scm-server/bin/scm-server \
 && chown scm:scm ${SCM_HOME} ${CACHE_DIR}

WORKDIR "/opt/scm-server"
VOLUME ["${SCM_HOME}", "${CACHE_DIR}"]
EXPOSE 8080
USER scm

ENTRYPOINT [ "/opt/scm-server/bin/scm-server" ]
