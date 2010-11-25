#!/usr/bin/env ${python}

import os
repositoryPath = os.environ['SCM_REPOSITORY_PATH']

from mercurial import demandimport; demandimport.enable()
from mercurial.hgweb import hgweb, wsgicgi
application = hgweb(repositoryPath)
wsgicgi.launch(application)
