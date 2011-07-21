#!/usr/bin/env ${python}

import os, sys, urllib

pythonPath = "${path}"

if len(pythonPath) > 0:
  pathParts = pythonPath.split(os.pathsep)
  for i in range(len(pathParts)):
    sys.path.insert(i, pathParts[i])

baseUrl = "${url}"

def scmhook(ui, repo, hooktype, node=None, source=None, **kwargs):
  url = baseUrl + os.path.basename(repo.root) + "/" + hooktype
  conn = urllib.urlopen(url);
  # todo validate (if conn.code == 200:)
