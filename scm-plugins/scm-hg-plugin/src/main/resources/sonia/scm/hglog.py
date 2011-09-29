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

import sys, os

pythonPath = os.environ['SCM_PYTHON_PATH']

if len(pythonPath) > 0:
  pathParts = pythonPath.split(os.pathsep)
  for i in range(len(pathParts)):
    sys.path.insert(i, pathParts[i])

from mercurial import hg, ui, commands
from mercurial.node import hex
from xml.sax.saxutils import escape
import datetime, time

repositoryPath = os.environ['SCM_REPOSITORY_PATH']
repo = hg.repository(ui.ui(), path = repositoryPath)

start =  os.environ['SCM_REVISION_START']
limit = os.environ['SCM_REVISION_LIMIT']

total = len(repo)
limit = int(limit)

try:
  start = int(start)
  startRev = total - start - 1
  if startRev < 0:
    startRev = 0
except ValueError:
  startRev = repo[start].rev()

endRev = startRev - limit
if endRev < -1:
  endRev = -1

# header
print '<changeset-paging>'
print '  <total>' + str(total) + '</total>'
print '  <changesets>'

# changesets
for i in range(startRev, endRev, -1):
  ctx = repo[i]
  time = int(ctx.date()[0]) * 1000
  branch = ctx.branch()
  tags = ctx.tags()
  status = repo.status(ctx.p1().node(), ctx.node())
  mods = status[0]
  added = status[1]
  deleted = status[2]
  authorName = ctx.user();
  authorMail = None
  
  if authorName:
    s = authorName.find('<')
    e = authorName.find('>')
    if s > 0 and e > 0:
      authorMail = authorName[s + 1:e].strip()
      authorName = authorName[0:s].strip()
  
  print '    <changeset>'
  print '      <id>' + str(i) + ':' + hex(ctx.node()[:6]) + '</id>'
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
    print '      <branches>'
    print '        <branch>' + branch + '</branch>'
    print '      </branches>'

  # tags
  if tags:
    print '      <tags>'
    for t in tags:
      print '        <tag>' + t + '</tag>'
    print '      </tags>'
    
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
    print '        <deleted>'
    for dele in deleted:
      print '          <file>' + dele + '</file>'
    print '        </deleted>'    
    
  print '      </modifications>'
  print '    </changeset>'
  
# footer
print '  </changesets>'
print '</changeset-paging>'
