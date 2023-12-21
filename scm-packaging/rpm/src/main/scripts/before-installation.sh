#!/bin/bash
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
current_version=$(yum info scm-server | grep Version | awk '{ print $3 }' | awk -F "" '{print $1}')

if [ $((current_version)) -lt  $(("3")) ]; then
	echo "

#########################################################

You are upgrading to a new major version which could break your current server
configuration. Find more information about the migration here:

https://scm-manager.org/docs/latest/en/migrate-scm-manager-from-v2/

Press Enter to continue

#########################################################

" >/dev/tty
  if exec </dev/tty; then
    read input;
  fi;
fi

getent group scm >/dev/null || groupadd -r scm
getent passwd scm >/dev/null || \
    useradd -r -g scm -M \
    -s /sbin/nologin -d /var/lib/scm \
    -c "user for the scm-server process" scm
exit 0
