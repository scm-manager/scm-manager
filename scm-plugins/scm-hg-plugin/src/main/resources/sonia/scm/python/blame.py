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
  blameLines = createChildNode(doc, blameResult, 'blamelines')
  linesCtx = repo[revision][path].annotate()
  lineNumber = 0
  for lineCtx in linesCtx:
    lineNumber += 1
    appendBlameLine(doc, blameLines, lineCtx, lineNumber)
  appendTextNode(doc, blameResult, 'total', str(lineNumber))

# main method

repo = openRepository()
revision = os.environ['SCM_REVISION']
path = os.environ['SCM_PATH']

doc = Document()
appendBlameLines(doc, repo, revision, path)
writeXml(doc)