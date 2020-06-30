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

# we need to build mercurial by ourself,
# because we need mercurial with python 2 for now
# TODO remove if python3 support is available for scm-manager hg extensions
FROM adoptopenjdk/openjdk11:jdk-11.0.7_10-alpine-slim as build
WORKDIR /src
RUN apk add --upgrade alpine-sdk python2 python2-dev
RUN set -x \
 && wget https://www.mercurial-scm.org/release/mercurial-4.9.1.tar.gz \
 && tar xvfz mercurial-4.9.1.tar.gz \
 && rm -f mercurial-4.9.1.tar.gz
WORKDIR /src/mercurial-4.9.1
RUN make build
RUN make PREFIX=/usr install-bin

FROM adoptopenjdk/openjdk11:jdk-11.0.7_10-alpine-slim
ENV SCM_HOME=/var/lib/scm
ENV CACHE_DIR=/var/cache/scm/work

COPY . /

RUN set -x \
 && apk add --no-cache python2 bash ca-certificates \
 && addgroup -S -g 1000 scm \
 && adduser -S -s /bin/false -G scm -h ${SCM_HOME} -D -H -u 1000 scm \
 && mkdir -p ${SCM_HOME} ${CACHE_DIR} \
 && chmod +x /opt/scm-server/bin/scm-server \
 && chown scm:scm ${SCM_HOME} ${CACHE_DIR}

# copy mercurial installation
COPY --from=build /usr/bin/hg /usr/bin/hg
COPY --from=build /usr/lib/python2.7 /usr/lib/python2.7

WORKDIR "/opt/scm-server"
VOLUME ["${SCM_HOME}", "${CACHE_DIR}"]
EXPOSE 8080
USER scm

ENTRYPOINT [ "/opt/scm-server/bin/scm-server" ]
