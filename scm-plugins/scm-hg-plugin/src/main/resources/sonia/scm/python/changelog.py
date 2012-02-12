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
  appendChangesetNode(doc, doc, ctx)
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
