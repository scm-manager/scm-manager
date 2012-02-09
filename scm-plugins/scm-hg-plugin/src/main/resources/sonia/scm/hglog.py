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


# import basic modules
import sys, os

# create python path
pythonPath = os.environ['SCM_PYTHON_PATH']

if len(pythonPath) > 0:
  pathParts = pythonPath.split(os.pathsep)
  for i in range(len(pathParts)):
    sys.path.insert(i, pathParts[i])

# import mercurial modules
from mercurial import hg, ui, commands
from mercurial.node import hex
from xml.dom.minidom import Document

# util methods
def openRepository():
  repositoryPath = os.environ['SCM_REPOSITORY_PATH']
  return hg.repository(ui.ui(), path = repositoryPath)

def writeXml(doc):
  # print doc.toprettyxml(indent="  ")
  print doc.toxml()

def createChildNode(doc, parentNode, name):
  node = doc.createElement(name)
  parentNode.appendChild(node)
  return node

def appendValue(doc, node, value):
  textNode = doc.createTextNode(value)
  node.appendChild(textNode)
  
def appendTextNode(doc, parentNode, name, value):
  node = createChildNode(doc, parentNode, name)
  appendValue(doc, node, value)

def appendListNodes(doc, parentNode, name, values):
  if values:
    for value in values:
      appendTextNode(doc, parentNode, name, value)

def appendWrappedListNodes(doc, parentNode, wrapperName, name, values):
  if values:
    wrapperNode = createChildNode(doc, parentNode, wrapperName)
    appendListNodes(doc, wrapperNode, name, values)

# changeset methods

def getId(ctx):
  return str(ctx.rev()) + ':' + hex(ctx.node()[:6])

def appendIdNode(doc, parentNode, ctx):
  id = getId(ctx)
  appendTextNode(doc, parentNode, 'id', id)

def appendParentNodes(doc, parentNode, ctx):
  parents = ctx.parents()
  if parents:
    for parent in parents:
      parentId = getId(parent)
      appendTextNode(doc, parentNode, 'parents', parentId)
      
def appendDateNode(doc, parentNode, ctx):
  time = int(ctx.date()[0]) * 1000
  date = str(time).split('.')[0]
  appendTextNode(doc, parentNode, 'date', date)

def appendAuthorNodes(doc, parentNode, ctx):
  authorName = ctx.user()
  authorMail = None
  if authorName:
    authorNode = createChildNode(doc, parentNode, 'author')
    s = authorName.find('<')
    e = authorName.find('>')
    if s > 0 and e > 0:
      authorMail = authorName[s + 1:e].strip()
      authorName = authorName[0:s].strip()
      appendTextNode(doc, authorNode, 'mail', authorMail)
      
    appendTextNode(doc, authorNode, 'name', authorName)
    
def appendBranchesNode(doc, parentNode, ctx):
  branch = ctx.branch()
  if branch != 'default':
    appendTextNode(doc, parentNode, 'branches', branch)
    
def appendModifications(doc, parentNode, ctx):
  status = repo.status(ctx.p1().node(), ctx.node())
  if status:
    modificationsNode = createChildNode(doc, parentNode, 'modifications')
    appendWrappedListNodes(doc, modificationsNode, 'added', 'file', status[1])
    appendWrappedListNodes(doc, modificationsNode, 'modified', 'file', status[0])
    appendWrappedListNodes(doc, modificationsNode, 'removed', 'file', status[2])
  
def appendChangesetNode(doc, parentNode, ctx):
  changesetNode = createChildNode(doc, parentNode, 'changeset')
  appendIdNode(doc, changesetNode, ctx)
  appendParentNodes(doc, changesetNode, ctx)
  appendTextNode(doc, changesetNode, 'description', ctx.description())
  appendDateNode(doc, changesetNode, ctx)
  appendAuthorNodes(doc, changesetNode, ctx)
  appendBranchesNode(doc, changesetNode, ctx)
  appendListNodes(doc, changesetNode, 'tags', ctx.tags())
  appendModifications(doc, changesetNode, ctx)
  
# changeset methods end

# change log methods

def createBasicNodes(doc, ctxs):
  rootNode = doc.createElement('changeset-paging')
  doc.appendChild(rootNode)
  total = str(len(repo))
  appendTextNode(doc, rootNode, 'total', total)
  return createChildNode(doc, rootNode, 'changesets')
  
def appendChangesetsForPath(doc, repo, rev, path):
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
  # handle paging
  start = os.environ['SCM_PAGE_START']
  limit = os.environ['SCM_PAGE_LIMIT']
  if len(start) > 0:
    revs = revs[int(start):]
  if len(limit) > 0:
    revs = revs[:int(limit)]
  # output
  changesets = createBasicNodes(doc, revs)
  for ctx in revs:
    appendChangesetNode(doc, changesets, ctx)
  
def appendChangesetsForStartAndEnd(doc, repo, startRev, endRev):
  changesets = createBasicNodes(doc, repo)
  for i in range(endRev, startRev, -1):
    appendChangesetNode(doc, changesets, repo[i])
    
# change log methods
  
# main method

repo = openRepository()
doc = Document()

path = os.environ['SCM_PATH']
startNode = os.environ['SCM_REVISION_START']
endNode = os.environ['SCM_REVISION_END']
rev = os.environ['SCM_REVISION']

if len(path) > 0:
  appendChangesetsForPath(doc, repo, rev, path)
elif len(rev) > 0:
  ctx = repo[rev]
  changesets = createBasicNodes(doc, repo)
  appendChangesetNode(doc, changesets, repo, ctx)
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
  appendChangesetsForStartAndEnd(doc, repo, startRev, endRev)
  
# write document
writeXml(doc)
