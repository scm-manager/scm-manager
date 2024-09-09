#!/bin/bash
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
