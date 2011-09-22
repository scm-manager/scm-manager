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

pythonPath = ''
# pythonPath = os.environ['SCM_PYTHON_PATH']

if len(pythonPath) > 0:
  pathParts = pythonPath.split(os.pathsep)
  for i in range(len(pathParts)):
    sys.path.insert(i, pathParts[i])


from mercurial import hg, ui, commands
from xml.sax.saxutils import escape
import datetime, time

repositoryPath = os.environ['SCM_REPOSITORY_PATH']

revision = os.environ['SCM_REVISION']
path = os.environ['SCM_PATH']

repo = hg.repository(ui.ui(), path = repositoryPath)
ctx = repo[revision]
fctx = ctx[path]

c = 0
lines = fctx.annotate()
print '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
print '<blame-result>'
print '  <blamelines>'
for line in lines:
  ctx = line[0].changectx()
  time = int(ctx.date()[0]) * 1000
  authorName = ctx.user();
  authorMail = None
  
  if authorName:
    start = authorName.find('<')
    end = authorName.find('>')
    if start > 0 and end > 0:
      authorMail = authorName[start + 1:end].strip()
      authorName = authorName[0:start].strip()
  
  c += 1
  print '    <blameline>'
  print '      <lineNumber>' + str(c) + '</lineNumber>'
  # get revision id not the number
  print '      <revision>' + str(ctx.rev()) + '</revision>'
  print '      <when>' + str(time).split('.')[0] + '</when>'
  if authorName:
    print '      <author>'
    print '        <name>' + authorName + '</name>'
    if authorMail:
      print '        <mail>' + authorMail + '</mail>'
    print '      </author>'
  print '      <description>' + escape(ctx.description()) + '</description>'
  print '      <code>' + escape(line[1][:-1]) + '</code>'
  print '    </blameline>'
  
print '  </blamelines>'
print '  <total>' + str(c) + '</total>'
print '</blame-result>'
