#!/bin/sh
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
