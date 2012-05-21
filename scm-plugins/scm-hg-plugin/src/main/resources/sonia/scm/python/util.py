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
  return str(ctx.rev()) + ':' + hex(ctx.node()[:6])
  
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
