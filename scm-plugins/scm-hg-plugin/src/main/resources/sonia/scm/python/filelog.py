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

class SubRepository:
  url = None
  revision = None

def getName(path):
  parts = path.split('/')
  length = len(parts)
  if path.endswith('/'):
    length =- 1
  return parts[length - 1]

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
  appendTextNode(doc, fileNode, 'path', path)
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
    for f in mf:
      if f.startswith(path):
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
