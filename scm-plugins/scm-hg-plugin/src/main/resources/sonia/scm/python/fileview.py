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

"""fileview

Prints date, size and last message of files.
"""

from collections import defaultdict
from mercurial import scmutil
from builtins import open, bytes

cmdtable = {}

try:
    from mercurial import registrar
    command = registrar.command(cmdtable)
except (AttributeError, ImportError):
    # Fallback to hg < 4.3 support
    from mercurial import cmdutil
    command = cmdutil.command(cmdtable)

try:
    from mercurial.utils import dateutil
    _parsedate = dateutil.parsedate
except ImportError:
    # compat with hg < 4.6
    from mercurial import util
    _parsedate = util.parsedate

FILE_MARKER = b'<files>'

class File_Collector:

  def __init__(self, recursive = False):
    self.recursive = recursive
    self.structure = defaultdict(dict, ((FILE_MARKER, []),))

  def collect(self, paths, path = "", dir_only = False):
    for p in paths:
      if p.startswith(path + b"/") or path == b"":
        self.attach(self.extract_name_without_parent(path, p), self.structure, dir_only)

  def attach(self, branch, trunk, dir_only = False):
    parts = branch.split(b'/', 1)
    if len(parts) == 1:  # branch is a file
      if dir_only:
        trunk[parts[0]] = defaultdict(dict, ((FILE_MARKER, []),))
      else:
        trunk[FILE_MARKER].append(parts[0])
    else:
      node, others = parts
      if node not in trunk:
        trunk[node] = defaultdict(dict, ((FILE_MARKER, []),))
      if self.recursive:
        self.attach(others, trunk[node], dir_only)

  def extract_name_without_parent(self, parent, name_with_parent):
    if len(parent) > 0:
      name_without_parent = name_with_parent[len(parent):]
      if name_without_parent.startswith(b"/"):
          name_without_parent = name_without_parent[1:]
      return name_without_parent
    return name_with_parent

class File_Object:
  def __init__(self, directory, path):
    self.directory = directory
    self.path = path
    self.children = []
    self.sub_repository = None

  def get_name(self):
    parts = self.path.split(b"/")
    return parts[len(parts) - 1]

  def get_parent(self):
    idx = self.path.rfind(b"/")
    if idx > 0:
      return self.path[0:idx]
    return ""

  def add_child(self, child):
    self.children.append(child)

  def __getitem__(self, key):
    return self.children[key]

  def __len__(self):
    return len(self.children)

  def __repr__(self):
    result = self.path
    if self.directory:
      result += b"/"
    return result

class File_Walker:

  def __init__(self, sub_repositories, visitor):
    self.visitor = visitor
    self.sub_repositories = sub_repositories

  def create_file(self, path):
    return File_Object(False, path)

  def create_directory(self, path):
    directory = File_Object(True, path)
    if path in self.sub_repositories:
      directory.sub_repository = self.sub_repositories[path]
    return directory

  def visit_file(self, path):
    file = self.create_file(path)
    self.visit(file)

  def visit_directory(self, path):
    file = self.create_directory(path)
    self.visit(file)

  def visit(self, file):
    self.visitor.visit(file)

  def create_path(self, parent, path):
    if len(parent) > 0:
      return parent + b"/" + path
    return path

  def walk(self, structure, parent = b""):
    if hasattr(structure, "iteritems"):
      items = structure.iteritems()
    else:
      items = structure.items()
    sortedItems = sorted(items, key = lambda item: self.sortKey(item))
    for key, value in sortedItems:
      if key == FILE_MARKER:
        if value:
          for v in value:
            self.visit_file(self.create_path(parent, v))
      else:
        self.visit_directory(self.create_path(parent, key))
        if isinstance(value, dict):
          self.walk(value, self.create_path(parent, key))
        else:
          self.visit_directory(self.create_path(parent, value))

  def sortKey(self, item):
    if (item[0] == FILE_MARKER):
      return b"2"
    else:
      return b"1" + item[0]

class SubRepository:
  url = None
  revision = None

def collect_sub_repositories(revCtx):
  subrepos = {}
  try:
    hgsub = revCtx.filectx(b'.hgsub').data().split(b'\n')
    for line in hgsub:
      parts = line.split(b'=')
      if len(parts) > 1:
        subrepo = SubRepository()
        subrepo.url = parts[1].strip()
        subrepos[parts[0].strip()] = subrepo
  except Exception:
    pass

  try:
    hgsubstate = revCtx.filectx(b'.hgsubstate').data().split(b'\n')
    for line in hgsubstate:
      parts = line.split(b' ')
      if len(parts) > 1:
        subrev = parts[0].strip()
        subrepo = subrepos[parts[1].strip()]
        subrepo.revision = subrev
  except Exception:
    pass

  return subrepos

class Writer:
  def __init__(self, ui):
    self.ui = ui

  def write(self, value):
    self.ui.write(value)

class File_Printer:

  def __init__(self, writer, repo, revCtx, disableLastCommit, transport, limit, offset):
    self.writer = writer
    self.repo = repo
    self.revCtx = revCtx
    self.disableLastCommit = disableLastCommit
    self.transport = transport
    self.result_count = 0
    self.initial_path_printed = False
    self.limit = limit
    self.offset = offset

  def print_directory(self, path):
    if not self.initial_path_printed or self.offset == 0 or self.shouldPrintResult():
      self.initial_path_printed = True
      format = b'%s/\n'
      if self.transport:
          format = b'd%s/\0'
      self.writer.write( format % path)

  def print_file(self, path):
    self.result_count += 1
    if self.shouldPrintResult():
      file = self.revCtx[path]
      date = b'0 0'
      description = b'n/a'
      if not self.disableLastCommit:
        linkrev = self.repo[file.linkrev()]
        date = b'%d %d' % _parsedate(linkrev.date())
        description = linkrev.description()
      format = b'%s %i %s %s\n'
      if self.transport:
        format = b'f%s\n%i %s %s\0'
      self.writer.write( format % (file.path(), file.size(), date, description) )

  def print_sub_repository(self, path, subrepo):
    self.result_count += 1
    if self.shouldPrintResult():
      format = b'%s/ %s %s\n'
      if self.transport:
        format = b's%s/\n%s %s\0'
      self.writer.write( format % (path, subrepo.revision, subrepo.url))

  def visit(self, file):
    if file.sub_repository:
      self.print_sub_repository(file.path, file.sub_repository)
    elif file.directory:
      self.print_directory(file.path)
    else:
      self.print_file(file.path)

  def shouldPrintResult(self):
    return self.offset < self.result_count <= self.limit + self.offset

  def isTruncated(self):
    return self.result_count > self.limit + self.offset

  def finish(self):
    if self.isTruncated():
      if self.transport:
        self.writer.write(b"t")
      else:
        self.writer.write(b"truncated")

class File_Viewer:
  def __init__(self, revCtx, visitor):
    self.revCtx = revCtx
    self.visitor = visitor
    self.sub_repositories = {}
    self.recursive = False

  def remove_ending_slash(self, path):
    if path.endswith(b"/"):
        return path[:-1]
    return path

  def view(self, path = b""):
    manifest = self.revCtx.manifest()
    if len(path) > 0 and path in manifest:
      self.visitor.visit(File_Object(False, path))
    else:
      p = self.remove_ending_slash(path)

      collector = File_Collector(self.recursive)
      walker = File_Walker(self.sub_repositories, self.visitor)

      self.visitor.visit(File_Object(True, p))
      collector.collect(manifest, p)
      collector.collect(self.sub_repositories.keys(), p, True)
      walker.walk(collector.structure, p)

@command(b'fileview', [
    (b'r', b'revision', b'tip', b'revision to print'),
    (b'p', b'path', b'', b'path to print'),
    (b'c', b'recursive', False, b'browse repository recursive'),
    (b'd', b'disableLastCommit', False, b'disables last commit description and date'),
    (b's', b'disableSubRepositoryDetection', False, b'disables detection of sub repositories'),
    (b't', b'transport', False, b'format the output for command server'),
    (b'l', b'limit', 100, b'limit the number of results'),
    (b'o', b'offset', 0, b'proceed from the given result number (zero based)'),
  ])
def fileview(ui, repo, **opts):
  revCtx = scmutil.revsingle(repo, opts["revision"])
  subrepos = {}
  if not opts["disableSubRepositoryDetection"]:
    subrepos = collect_sub_repositories(revCtx)
  writer = Writer(ui)
  printer = File_Printer(writer, repo, revCtx, opts["disableLastCommit"], opts["transport"], opts["limit"], opts["offset"])
  viewer = File_Viewer(revCtx, printer)
  viewer.recursive = opts["recursive"]
  viewer.sub_repositories = subrepos
  viewer.view(opts["path"])
  printer.finish()
