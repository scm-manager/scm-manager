#
# Copyright (c) 2025 - present Cloudogu GmbH
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

"""Add a ``--config-file`` option, prior to support for it in Mercurial 7.0

This extension wraps every command with processing for one or more optional
``--config-file HGRC`` arguments.  Like the core functionality introduced in hg
7.0, the configuration stored in these files overwrites any of the same config
options that were stored in system, user, or repo level config files.  Only
``--config`` options will take priority over the config options stored in these
files.  If multiple files are specified, the config in the last file overrides
the config in any previous file.

The limitation with this extension is that it can't introduce a truly global
option like hg 7.0 has.  Therefore, the option must be given after the command
verb.  Mercurial 7.0 can process this option before or after the verb.  This
extension detects if Mercurial already supports the option, and does nothing in
that case.
"""

from mercurial import (
    commands,
    config as configmod,
    error,
    exthelper,
    extensions,
    pycompat,
)

from mercurial.i18n import _

# 6.0 was released on Nov 2021; 6.1.4 is the last Python 2 release.
testedwith = b'6.0 6.0.3 6.1.4 6.2.3 6.3.3 6.4.5 6.5.3 6.6.3 6.7.4 6.8.2 6.9.1'

eh = exthelper.exthelper()
extsetup = eh.finalextsetup


@eh.extsetup
def _extsetup(ui):
    # Mercurial started providing a global --config-file option starting with
    # 7.0.  The command line API is guaranteed to be stable over time, but the
    # internal APIs are not.  Therefore, look for the global option before doing
    # anything, and bail out of the setup if it is present.  If something goes
    # wrong with the search, assume that something was refactored in a post-7.0
    # world, and also skip the setup since it will be supported natively.
    try:
        for opt in commands.globalopts:
            if opt[1] == b'config-file':
                return
    except (AttributeError, IndexError):
        return

    # There is no global option, so patch all commands to add --config-file.
    # Unlike a global option (which can appear anywhere), this must appear
    # after the command verb.

    for cmd, impl in commands.table.items():
        # Some commands have aliases appended with a '|', and can't be wrapped
        # with that name.
        if b'|' in cmd:
            cmd = cmd.split(b'|')[0]

        addopt = [
            (
                b'',
                b'config-file',
                [],
                b'load config file to set/override config options',
                b'HGRC',
            )
        ]

        # Wrap the command function to handle the option
        entry = extensions.wrapcommand(commands.table, cmd, _wrapper)
        entry[1].extend(addopt)


def _wrapper(orig, ui, *args, **kwargs):
    configs = _parse_config_files(pycompat.sysargv, kwargs.pop('config_file'))

    overrides = {}

    # The --config option overrides --config-file contents in the global parser.
    # But since that's all been populated in the ui object at this point, take
    # care to not replace those items.
    for section, name, value, src in configs:
        if ui.configsource(section, name) != b'--config':
            overrides[(section, name)] = value

    with ui.configoverride(overrides, b"--config-file"):
        return orig(ui, *args, **kwargs)


def _parse_config_files(cmdargs, config_files):
    """parse the --config-file options from the command line

    A list of tuples containing (section, name, value, source) is returned,
    in the order they were read.
    """

    configs = []

    cfg = configmod.config()

    for file in config_files:
        try:
            cfg.read(file)
        except error.ConfigError as e:
            raise error.InputError(
                _(b'invalid --config-file content at %s') % e.location,
                hint=e.message,
            )
        except FileNotFoundError:
            hint = None
            if b'--cwd' in cmdargs:
                hint = _(b"this file is resolved before --cwd is processed")

            raise error.InputError(
                _(b'missing file "%s" for --config-file') % file, hint=hint
            )

    for section in cfg.sections():
        for item in cfg.items(section):
            name = item[0]
            value = item[1]
            src = cfg.source(section, name)

            configs.append((section, name, value, src))

    return configs
