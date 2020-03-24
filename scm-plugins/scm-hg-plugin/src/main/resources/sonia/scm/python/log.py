#
# MIT License
#
# Copyright (c) 2020-present Cloudogu GmbH and Contributors
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.
#


import os
from util import *
from xml.dom.minidom import Document

# changeset methods

def appendIdNode(doc, parentNode, ctx):
  id = getId(ctx)
  appendTextNode(doc, parentNode, 'id', id)

def appendParentNodes(doc, parentNode, ctx):
  parents = ctx.parents()
  if parents:
    for parent in parents:
      parentId = getId(parent)
      appendTextNode(doc, parentNode, 'parents', parentId)
    
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
  appendDateNode(doc, changesetNode, 'date', ctx.date())
  appendAuthorNodes(doc, changesetNode, ctx)
  appendBranchesNode(doc, changesetNode, ctx)
  appendListNodes(doc, changesetNode, 'tags', ctx.tags())
  appendModifications(doc, changesetNode, ctx)
  
# changeset methods end

# change log methods

def createBasicNodes(doc, ctxs):
  rootNode = doc.createElement('changeset-paging')
  doc.appendChild(rootNode)
  total = str(len(ctxs))
  appendTextNode(doc, rootNode, 'total', total)
  return createChildNode(doc, rootNode, 'changesets')

def collectChangesets(repo, path, startNode, endNode):
  start = 'tip'
  end = '0'
  if len(startNode) > 0:
    start = startNode
  if len(endNode) > 0:
    end = endNode

  ctxs = []
  startRev = repo[start].rev()
  endRev = repo[end].rev() - 1

  onlyWithPath = len(path) > 0

  for i in range(startRev, endRev, -1):
    ctx = repo[i]
    if onlyWithPath:
      if path in ctx.files():
        ctxs.append(ctx)
    else:
      ctxs.append(ctx)

  return ctxs

def stripChangesets(ctxs, start, limit):
  if limit < 0:
    ctxs = ctxs[start:]
  else:
    limit = limit + start
    if limit > len(ctxs):
      ctxs = ctxs[start:]
    else:
      ctxs = ctxs[start:limit]
  return ctxs
    
# change log methods
  
# main method
repo = openRepository()
doc = Document()

# parameter
path = os.environ['SCM_PATH']
startNode = os.environ['SCM_REVISION_START']
endNode = os.environ['SCM_REVISION_END']
rev = os.environ['SCM_REVISION']
# paging parameter
start = int(os.environ['SCM_PAGE_START'])
limit = int(os.environ['SCM_PAGE_LIMIT'])

if len(rev) > 0:
  ctx = repo[rev]
  appendChangesetNode(doc, doc, ctx)
else:
  ctxs = collectChangesets(repo, path, startNode, endNode)
  changesetsNode = createBasicNodes(doc, ctxs)
  ctxs = stripChangesets(ctxs, start, limit)
  for ctx in ctxs:
    appendChangesetNode(doc, changesetsNode, ctx)


# write document
writeXml(doc)
