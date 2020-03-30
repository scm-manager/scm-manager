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

# import basic modules
import sys, os

# import mercurial modules
from mercurial import hg, ui, commands, encoding
from mercurial.node import hex
from xml.dom.minidom import Document

# util methods
def openRepository():
  repositoryPath = os.environ['SCM_REPOSITORY_PATH']
  return hg.repository(ui.ui(), path = repositoryPath)

def writeXml(doc):
  # print doc.toprettyxml(indent="  ")
  doc.writexml(sys.stdout, encoding='UTF-8')

def createChildNode(doc, parentNode, name):
  node = doc.createElement(name)
  parentNode.appendChild(node)
  return node

def appendValue(doc, node, value):
  textNode = doc.createTextNode(encoding.tolocal(value))
  node.appendChild(textNode)
  
def appendTextNode(doc, parentNode, name, value):
  node = createChildNode(doc, parentNode, name)
  appendValue(doc, node, value)
  
def appendDateNode(doc, parentNode, nodeName, date):
  time = int(date[0]) * 1000
  date = str(time).split('.')[0]
  appendTextNode(doc, parentNode, nodeName, date)
  
def appendListNodes(doc, parentNode, name, values):
  if values:
    for value in values:
      appendTextNode(doc, parentNode, name, value)

def appendWrappedListNodes(doc, parentNode, wrapperName, name, values):
  if values:
    wrapperNode = createChildNode(doc, parentNode, wrapperName)
    appendListNodes(doc, wrapperNode, name, values)
    
def getId(ctx):
  id = ''
  if os.environ['SCM_ID_REVISION'] == 'true':
    id = str(ctx.rev()) + ':'
  return id + hex(ctx.node())
  
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
