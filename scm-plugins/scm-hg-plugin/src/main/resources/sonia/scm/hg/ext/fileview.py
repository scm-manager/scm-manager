#
# Copyright (c) 2010, Sebastian Sdorra
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# 1. Redistributions of source code must retain the above copyright notice,
#    this list of conditions and the following disclaimer.
# 2. Redistributions in binary form must reproduce the above copyright notice,
#    this list of conditions and the following disclaimer in the documentation
#    and/or other materials provided with the distribution.
# 3. Neither the name of SCM-Manager; nor the names of its
#    contributors may be used to endorse or promote products derived from this
#    software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
# DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
# (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
# LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
# ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
# SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#
# http://bitbucket.org/sdorra/scm-manager
#
#
"""fileview

Prints date, size and last message of files.
"""

from collections import defaultdict
from mercurial import cmdutil,util

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

FILE_MARKER = '<files>'

class File_Collector:

  def __init__(self, recursive = False):
    self.recursive = recursive
    self.structure = defaultdict(dict, ((FILE_MARKER, []),))

  def collect(self, paths, path = "", dir_only = False):
    for p in paths:
      if p.startswith(path):
        self.attach(self.extract_name_without_parent(path, p), self.structure, dir_only)

  def attach(self, branch, trunk, dir_only = False):
    parts = branch.split('/', 1)
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
      if name_without_parent.startswith("/"):
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
    parts = self.path.split("/")
    return parts[len(parts) - 1]

  def get_parent(self):
    idx = self.path.rfind("/")
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
      result += "/"
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
      return parent + "/" + path
    return path

  def walk(self, structure, parent = ""):
    for key, value in structure.iteritems():
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

class SubRepository:
  url = None
  revision = None

def collect_sub_repositories(revCtx):
  subrepos = {}
  try:
    hgsub = revCtx.filectx('.hgsub').data().split('\n')
    for line in hgsub:
      parts = line.split('=')
      if len(parts) > 1:
        subrepo = SubRepository()
        subrepo.url = parts[1].strip()
        subrepos[parts[0].strip()] = subrepo
  except Exception:
    pass

  try:
    hgsubstate = revCtx.filectx('.hgsubstate').data().split('\n')
    for line in hgsubstate:
      parts = line.split(' ')
      if len(parts) > 1:
        subrev = parts[0].strip()
        subrepo = subrepos[parts[1].strip()]
        subrepo.revision = subrev
  except Exception:
    pass

  return subrepos

class File_Printer:

  def __init__(self, ui, repo, revCtx, disableLastCommit, transport):
    self.ui = ui
    self.repo = repo
    self.revCtx = revCtx
    self.disableLastCommit = disableLastCommit
    self.transport = transport

  def print_directory(self, path):
    format = '%s/\n'
    if self.transport:
        format = 'd%s/\0'
    self.ui.write( format % path)

  def print_file(self, path):
    file = self.revCtx[path]
    date = '0 0'
    description = 'n/a'
    if not self.disableLastCommit:
      linkrev = self.repo[file.linkrev()]
      date = '%d %d' % _parsedate(linkrev.date())
      description = linkrev.description()
    format = '%s %i %s %s\n'
    if self.transport:
      format = 'f%s\n%i %s %s\0'
    self.ui.write( format % (file.path(), file.size(), date, description) )

  def print_sub_repository(self, path, subrepo):
    format = '%s/ %s %s\n'
    if self.transport:
      format = 's%s/\n%s %s\0'
    self.ui.write( format % (path, subrepo.revision, subrepo.url))

  def visit(self, file):
    if file.sub_repository:
      self.print_sub_repository(file.path, file.sub_repository)
    elif file.directory:
      self.print_directory(file.path)
    else:
      self.print_file(file.path)

class File_Viewer:
  def __init__(self, revCtx, visitor):
    self.revCtx = revCtx
    self.visitor = visitor
    self.sub_repositories = {}
    self.recursive = False

  def remove_ending_slash(self, path):
    if path.endswith("/"):
        return path[:-1]
    return path

  def view(self, path = ""):
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

@command('fileview', [
    ('r', 'revision', 'tip', 'revision to print'),
    ('p', 'path', '', 'path to print'),
    ('c', 'recursive', False, 'browse repository recursive'),
    ('d', 'disableLastCommit', False, 'disables last commit description and date'),
    ('s', 'disableSubRepositoryDetection', False, 'disables detection of sub repositories'),
    ('t', 'transport', False, 'format the output for command server'),
  ])
def fileview(ui, repo, **opts):
  revCtx = repo[opts["revision"]]
  subrepos = {}
  if not opts["disableSubRepositoryDetection"]:
    subrepos = collect_sub_repositories(revCtx)
  printer = File_Printer(ui, repo, revCtx, opts["disableLastCommit"], opts["transport"])
  viewer = File_Viewer(revCtx, printer)
  viewer.recursive = opts["recursive"]
  viewer.sub_repositories = subrepos
  viewer.view(opts["path"])
