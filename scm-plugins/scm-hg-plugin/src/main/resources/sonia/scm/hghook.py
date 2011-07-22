#!/usr/bin/env ${python}

#
# registration .hg/hgrc:
#
# [hooks]
# changegroup.scm = python:scmhooks.callback
#

import os, sys, urllib

pythonPath = "${path}"

if len(pythonPath) > 0:
  pathParts = pythonPath.split(os.pathsep)
  for i in range(len(pathParts)):
    sys.path.insert(i, pathParts[i])

baseUrl = "${url}"

def callback(ui, repo, hooktype, node=None, source=None, **kwargs):
  if node != None:
    url = baseUrl + os.path.basename(repo.root) + "/" + hooktype + "?node=" + node
    conn = urllib.urlopen(url);
    if conn.code == 200:
      print( "scm-hook executed successfully" )
    else:
      print( "scm-hook failed" )
