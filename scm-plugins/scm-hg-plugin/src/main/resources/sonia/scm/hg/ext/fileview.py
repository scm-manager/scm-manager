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
# 3. Neither the name of hg-fileview; nor the names of its
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
# https://bitbucket.org/sdorra/hg-fileview
# 
# 
"""fileview

Prints date, size and last message of files.
"""
from mercurial import util

class SubRepository:
  url = None
  revision = None

def removeTrailingSlash(path):
  if path.endswith('/'):
    path = path[0:-1]
  return path
  
def appendTrailingSlash(path):
  if not path.endswith('/'):
    path += '/'
  return path

def collectFiles(revCtx, path, files, directories):
  length = 0
  paths = []
  mf = revCtx.manifest()
  if path is "":
    length = 1
    for f in mf:
      paths.append(f)
  else:
    length = len(path.split('/')) + 1
    directory = path
    if not directory.endswith('/'):
       directory += '/'
       
    for f in mf:
      if f.startswith(directory):
        paths.append(f)
  
  for p in paths:
    parts = p.split('/')
    depth = len(parts)
    if depth is length:
      file = revCtx[p]
      files.append(file)
    elif depth > length:
      dirpath = ''
      for i in range(0, length):
        dirpath += parts[i] + '/'
      if not dirpath in directories:
        directories.append(dirpath)
        
def createSubRepositoryMap(revCtx):
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
  
def printSubRepository(ui, path, subrepository, transport):
  format = '%s %s %s\n'
  if transport:
    format = 's%s\n%s %s\0'
  ui.write( format % (appendTrailingSlash(path), subrepository.revision, subrepository.url))
  
def printDirectory(ui, path, transport):
  format = '%s\n'
  if transport:
    format = 'd%s\0'
  ui.write( format % path)
  
def printFile(ui, repo, file, transport):
  linkrev = repo[file.linkrev()]
  date = "%d %d" % util.parsedate(linkrev.date())
  format = '%s %i %s %s\n'
  if transport:
    format = 'f%s\n%i %s %s\0'
  ui.write( format % (file.path(), file.size(), date, linkrev.description()))

def fileview(ui, repo, **opts):
  files = []
  directories = []
  revision = opts['revision']
  if revision == None:
    revision = 'tip'
  revCtx = repo[revision]
  path = opts['path']
  if path.endswith('/'):
    path = path[0:-1]
  transport = opts['transport']
  collectFiles(revCtx, path, files, directories)
  subRepositories = createSubRepositoryMap(revCtx)
  for k, v in subRepositories.iteritems():
    if k.startswith(path):
      printSubRepository(ui, k, v, transport)
  for d in directories:
    printDirectory(ui, d, transport)
  for f in files:
    printFile(ui, repo, f, transport)
  
cmdtable = {
  # cmd name        function call
  'fileview': (fileview,[
    ('r', 'revision', 'tip', 'revision to print'),
    ('p', 'path', '', 'path to print'),
    ('t', 'transport', False, 'format the output for command server'),
  ])
}