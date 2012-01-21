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

# import basic packages
import sys, os

# create python path
pythonPath = os.environ['SCM_PYTHON_PATH']

if len(pythonPath) > 0:
  pathParts = pythonPath.split(os.pathsep)
  for i in range(len(pathParts)):
    sys.path.insert(i, pathParts[i])

# import mercurial packages
from mercurial import hg, ui, commands
from mercurial.node import hex
from xml.sax.saxutils import escape
import datetime, time

# header
def printHeader(total):
  print '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
  print '<changeset-paging>'
  print '  <total>' + str(total) + '</total>'
  print '  <changesets>'

# changeset
def printChangeset(repo, ctx):
  time = int(ctx.date()[0]) * 1000
  branch = ctx.branch()
  tags = ctx.tags()
  status = repo.status(ctx.p1().node(), ctx.node())
  mods = status[0]
  added = status[1]
  deleted = status[2]
  authorName = ctx.user()
  authorMail = None
  parents = ctx.parents()
  
  if authorName:
    s = authorName.find('<')
    e = authorName.find('>')
    if s > 0 and e > 0:
      authorMail = authorName[s + 1:e].strip()
      authorName = authorName[0:s].strip()
  
  print '    <changeset>'
  print '      <id>' + str(ctx.rev()) + ':' + hex(ctx.node()[:6]) + '</id>'
  if parents:
    for parent in parents:
      print '      <parents>' + str(parent.rev()) + ':' + hex(parent.node()[:6]) + '</parents>'
  print '      <author>' + escape(ctx.user()) + '</author>'
  print '      <description>' + escape(ctx.description()) + '</description>'
  print '      <date>' + str(time).split('.')[0] + '</date>'

  # author
  if authorName:
    print '      <author>'
    print '        <name>' + authorName + '</name>'
    if authorMail:
      print '        <mail>' + authorMail + '</mail>'
    print '      </author>'

  # branches
  if branch != 'default':
    print '      <branches>' + branch + '</branches>'

  # tags
  if tags:
    for t in tags:
      print '      <tags>' + t + '</tags>'
    
  # modifications
  print '      <modifications>'
  
  # files added 
  if added:
    print '        <added>'
    for add in added:
      print '          <file>' + add + '</file>'
    print '        </added>'

  # files modified
  if mods:
    print '        <modified>'
    for mod in mods:
      print '          <file>' + mod + '</file>'
    print '        </modified>'

  # files deleted
  if deleted:
    print '        <removed>'
    for dele in deleted:
      print '          <file>' + dele + '</file>'
    print '        </removed>'    
    
  print '      </modifications>'
  print '    </changeset>'

# footer
def printFooter():
  print '  </changesets>'
  print '</changeset-paging>'

def printChangesetsForPath(repo, rev, path):
  if len(rev) <= 0:
    rev = "tip"

  fctxs = repo[rev].filectx(path)
  maxRev = fctxs.rev()
  
  revs = []
  for i in fctxs.filelog():
    fctx = fctxs.filectx(i)
    if fctx.rev() <= maxRev:
      revs.append(fctx.changectx())
  
  # reverse changesets
  revs.reverse()
  
  total = len(revs)
  
  # handle paging
  start = os.environ['SCM_PAGE_START']
  limit = os.environ['SCM_PAGE_LIMIT']
  
  if len(start) > 0:
    revs = revs[int(start):]

  if len(limit) > 0:
    revs = revs[:int(limit)]

  # output
  printHeader(total)
  for ctx in revs:
    printChangeset(repo, ctx)
  printFooter()
  
def printChangesetsForStartAndEnd(repo, startRev, endRev):
  printHeader(len(repo))
  for i in range(endRev, startRev, -1):
    printChangeset(repo, repo[i])
  printFooter()

repositoryPath = os.environ['SCM_REPOSITORY_PATH']
repo = hg.repository(ui.ui(), path = repositoryPath)

path = os.environ['SCM_PATH']
startNode = os.environ['SCM_REVISION_START']
endNode = os.environ['SCM_REVISION_END']
rev = os.environ['SCM_REVISION']

if len(path) > 0:
  printChangesetsForPath(repo, rev, path)
elif len(rev) > 0:
  ctx = repo[rev]
  print '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
  printChangeset(repo, ctx)
else:
  if len(startNode) > 0 and len(endNode) > 0:
    # start and end revision
    startRev = repo[startNode].rev() -1
    endRev = repo[endNode].rev()
  else:
    # paging
    start = os.environ['SCM_PAGE_START']
    limit = os.environ['SCM_PAGE_LIMIT']

    limit = int(limit)

    end = int(start)
    endRev = len(repo) - end - 1

    startRev = endRev - limit

  # fix negative start revisions
  if startRev < -1:
    startRev = -1
  
  # print
  printChangesetsForStartAndEnd(repo, startRev, endRev)

