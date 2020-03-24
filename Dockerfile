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

FROM openjdk:8u212-alpine3.9

ENV SCM_HOME=/var/lib/scm

RUN set -x \
 && apk add --no-cache mercurial bash \
 && addgroup -S -g 1000 scm \
 && adduser -S -s /bin/false -G scm -h /opt/scm-server -D -H -u 1000 scm \
 && mkdir ${SCM_HOME} \
 && chown scm:scm ${SCM_HOME}

ADD scm-server/target/scm-server-app.tar.gz /opt
RUN chown -R scm:scm /opt/scm-server

WORKDIR /opt/scm-server
VOLUME [ "${SCM_HOME}", "/opt/scm-server/var/log" ]
EXPOSE 8080
USER scm

ENTRYPOINT [ "/opt/scm-server/bin/scm-server" ]
