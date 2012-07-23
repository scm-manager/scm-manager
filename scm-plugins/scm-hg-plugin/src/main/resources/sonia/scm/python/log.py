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
