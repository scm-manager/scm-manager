#!/bin/sh
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

# clear workdir after upgrade
# https://bitbucket.org/sdorra/scm-manager/issues/923/scmmanager-installed-from-debian-package

WORKDIR="/var/cache/scm/work/webapp"
if [ -d "${WORKDIR}" ]; then
  rm -rf "${WORKDIR}"
fi

# reload systemd and make service available
systemctl --system daemon-reload || true

# enable service
if ! systemctl is-enabled scm-server >/dev/null 
then
  systemctl enable scm-server
fi

# start or restart service
if systemctl is-active scm-server >/dev/null 
then
  systemctl restart scm-server
else
  systemctl start scm-server
fi
