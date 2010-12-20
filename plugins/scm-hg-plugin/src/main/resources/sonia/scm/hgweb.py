#!/usr/bin/env ${python}

import os, sys
pythonPath = os.environ['SCM_PYTHON_PATH']

if len(pythonPath) > 0:
  pathParts = pythonPath.split(os.pathsep)
  for i in range(len(pathParts)):
    sys.path.insert(i, pathParts[i])

repositoryPath = os.environ['SCM_REPOSITORY_PATH']

from mercurial import demandimport; demandimport.enable()
from mercurial.hgweb import hgweb, wsgicgi
application = hgweb(repositoryPath)
wsgicgi.launch(application)
