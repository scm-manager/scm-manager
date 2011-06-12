#!/usr/bin/env ${python}

pythonPath = os.environ['SCM_PYTHON_PATH']

if len(pythonPath) > 0:
  pathParts = pythonPath.split(os.pathsep)
  for i in range(len(pathParts)):
    sys.path.insert(i, pathParts[i])


from mercurial import hg, ui
import datetime, time

def getName(path):
  parts = path.split('/')
  length = len(parts)
  if path.endswith('/'):
    length =- 1
  return parts[length - 1]

repositoryPath = os.environ['SCM_REPOSITORY_PATH']

revision = os.environ['SCM_REVISION']
path = os.environ['SCM_PATH']
name = getName(path)
length = 0
paths = []
repo = hg.repository(ui.ui(), path = repositoryPath)
mf = repo[revision].manifest()

if path is "":
  length = 1
  for f in mf:
    paths.append(f)
else:
  length = len(path.split('/')) + 1
  for f in mf:
    if f.startswith(path):
      paths.append(f)

files = []
directories = []

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
    
print '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
print '<browser-result>'
print '  <revision>' + revision + '</revision>'
# todo print tag, and branch
print '  <file>'
print '    <path>' + path + '</path>'
print '    <name>' + name + '</name>'
print '    <directory>true</directory>'
print '    <children>'
for dir in directories:
  print '    <file>'
  print '      <name>' + getName(dir) + '</name>'
  print '      <path>' + dir + '</path>'
  print '      <directory>true</directory>'
  print '    </file>'
    
for file in files:
  print '    <file>'
  print '      <name>' + getName(file.path()) + '</name>'
  print '      <path>' + file.path() + '</path>'
  print '      <directory>false</directory>'
  print '      <length>' + str(file.size()) + '</length>'
  time = file.date()[0]
  print '      <lastModified>' + str(time).split('.')[0] + '</lastModified>'
  print '    </file>'
print '    </children>'
print '  </file>'
print '</browser-result>'
