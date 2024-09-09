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

from mercurial import util
import platform

cmdtable = {}

try:
    from mercurial import registrar
    command = registrar.command(cmdtable)
except (AttributeError, ImportError):
    # Fallback to hg < 4.3 support
    from mercurial import cmdutil
    command = cmdutil.command(cmdtable)

@command(b'scmversion', norepo=True)
def scmversion(ui, **opts):
  pythonVersion = platform.python_version().encode("utf-8")
  ui.write(b"python/%s mercurial/%s\n" % (pythonVersion, util.version()))
