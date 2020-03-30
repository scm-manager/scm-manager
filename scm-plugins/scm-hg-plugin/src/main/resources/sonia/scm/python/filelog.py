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

class SubRepository:
  url = None
  revision = None

def getName(path):
  parts = path.split('/')
  length = len(parts)
  if path.endswith('/'):
    length =- 1
  return parts[length - 1]

def removeTrailingSlash(path):
  if path.endswith('/'):
    path = path[0:-1]
  return path

def appendSubRepositoryNode(doc, parentNode, path, subRepositories):
  if path in subRepositories:
    subRepository = subRepositories[path]
    subRepositoryNode = createChildNode(doc, parentNode, 'subrepository')
    if subRepository.revision != None:
      appendTextNode(doc, subRepositoryNode, 'revision', subRepository.revision)
    appendTextNode(doc, subRepositoryNode, 'repository-url', subRepository.url)

def createBasicFileNode(doc, parentNode, path, directory):
  fileNode = createChildNode(doc, parentNode, 'file')
  appendTextNode(doc, fileNode, 'name', getName(path))
  appendTextNode(doc, fileNode, 'path', removeTrailingSlash(path))
  appendTextNode(doc, fileNode, 'directory', directory)
  return fileNode

def appendDirectoryNode(doc, parentNode, path, subRepositories):
  fileNode = createBasicFileNode(doc, parentNode, path, 'true')
  appendSubRepositoryNode(doc, fileNode, path, subRepositories)
  
def appendFileNode(doc, parentNode, repo, file):
  linkrev = repo[file.linkrev()]
  fileNode = createBasicFileNode(doc, parentNode, file.path(), 'false')
  appendTextNode(doc, fileNode, 'length', str(file.size()))
  appendDateNode(doc, fileNode, 'lastModified', linkrev.date())
  appendTextNode(doc, fileNode, 'description', linkrev.description())
  
def createSubRepositoryMap(revCtx):
  subrepos = {}
  try:
    hgsub = revCtx.filectx('.hgsub').data().split('\n')
    for line in hgsub:
      parts = line.split('=')
      if len(parts) > 1:
        subrepo = SubRepository()
        subrepo.url = parts[1].strip()
        subrepos[parts[0].strip()] = subrepo
  except Exception:
    pass
    
  try:
    hgsubstate = revCtx.filectx('.hgsubstate').data().split('\n')
    for line in hgsubstate:
      parts = line.split(' ')
      if len(parts) > 1:
        subrev = parts[0].strip()
        subrepo = subrepos[parts[1].strip()]
        subrepo.revision = subrev
  except Exception:
    pass
    
  return subrepos
  
def appendSubRepositoryDirectories(directories, subRepositories):
  for k, v in subRepositories.iteritems():
    if k.startswith(path):
      directories.append(k)

def collectFiles(repo, revCtx, files, directories):
  length = 0
  paths = []
  mf = revCtx.manifest()
  if path is "":
    length = 1
    for f in mf:
      paths.append(f)
  else:
    length = len(path.split('/')) + 1
    directory = path
    if not directory.endswith('/'):
       directory += '/'

    for f in mf:
      if f.startswith(directory):
        paths.append(f)
  
  for p in paths:
    parts = p.split('/')
    depth = len(parts)
    if depth is length:
      file = repo[revision][p]
      files.append(file)
    elif depth > length:
      dirpath = ''
      for i in range(0, length):
        dirpath += parts[i] + '/'
      if not dirpath in directories:
        directories.append(dirpath)
  
def appendFileNodes(doc, parentNode, repo, revision):
  files = []
  directories = []
  revCtx = repo[revision]
  subRepositories = createSubRepositoryMap(revCtx)
  appendSubRepositoryDirectories(directories, subRepositories)
  collectFiles(repo, revCtx, files, directories)
  for dir in directories:
    appendDirectoryNode(doc, parentNode, dir, subRepositories)
  for file in files:
    appendFileNode(doc, parentNode, repo, file)
  

# main method

repo = openRepository()
revision = os.environ['SCM_REVISION']
path = os.environ['SCM_PATH']

# create document and append nodes

doc = Document()
browserResultNode = createChildNode(doc, doc, 'browser-result')
appendTextNode(doc, browserResultNode, 'revision', revision)
filesNode = createChildNode(doc, browserResultNode, 'files')
appendFileNodes(doc, filesNode, repo, revision)
writeXml(doc)
