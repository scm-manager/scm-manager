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

def appendBlameLine(doc, parent, lineCtx, lineNumber):
  ctx = lineCtx[0].changectx()
  lineNode = createChildNode(doc, parent, 'blameline')
  appendTextNode(doc, lineNode, 'lineNumber', str(lineNumber))
  appendTextNode(doc, lineNode, 'revision', getId(ctx))
  appendDateNode(doc, lineNode, 'when', ctx.date())
  appendAuthorNodes(doc, lineNode, ctx)
  appendTextNode(doc, lineNode, 'description', ctx.description())
  appendTextNode(doc, lineNode, 'code', lineCtx[1][:-1])

def appendBlameLines(doc, repo, revision, path):
  blameResult = createChildNode(doc, doc, 'blame-result')
  linesCtx = repo[revision][path].annotate()
  lineNumber = 0
  for lineCtx in linesCtx:
    lineNumber += 1
    appendBlameLine(doc, blameResult, lineCtx, lineNumber)
  appendTextNode(doc, blameResult, 'total', str(lineNumber))

# main method

repo = openRepository()
revision = os.environ['SCM_REVISION']
path = os.environ['SCM_PATH']

doc = Document()
appendBlameLines(doc, repo, revision, path)
writeXml(doc)