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

#
# registration .hg/hgrc:
#
# [hooks]
# changegroup.scm = python:scmhooks.callback
#

import os, urllib, urllib2

baseUrl = os.environ['SCM_URL']
challenge = os.environ['SCM_CHALLENGE']
token = os.environ['SCM_BEARER_TOKEN']
xsrf = os.environ['SCM_XSRF']
repositoryId = os.environ['SCM_REPOSITORY_ID']

def printMessages(ui, msgs):
  for line in msgs:
    if line.startswith("_e") or line.startswith("_n"):
      line = line[2:];
    ui.warn('%s\n' % line.rstrip())

def callHookUrl(ui, repo, hooktype, node):
  abort = True
  try:
    url = baseUrl + hooktype
    ui.debug( "send scm-hook to " + url + " and " + node + "\n" )
    data = urllib.urlencode({'node': node, 'challenge': challenge, 'token': token, 'repositoryPath': repo.root, 'repositoryId': repositoryId})
    # open url but ignore proxy settings
    proxy_handler = urllib2.ProxyHandler({})
    opener = urllib2.build_opener(proxy_handler)
    req = urllib2.Request(url, data)
    req.add_header("X-XSRF-Token", xsrf)
    conn = opener.open(req)
    if 200 <= conn.code < 300:
      ui.debug( "scm-hook " + hooktype + " success with status code " + str(conn.code) + "\n" )
      printMessages(ui, conn)
      abort = False
    else:
      ui.warn( "ERROR: scm-hook failed with error code " + str(conn.code) + "\n" )
  except urllib2.URLError, e:
    msg = None
    # some URLErrors have no read method
    if hasattr(e, "read"):
      msg = e.read()
    elif hasattr(e, "code"):
      msg = "scm-hook failed with error code " + str(e.code) + "\n"
    else:
      msg = str(e)
    if len(msg) > 0:
      printMessages(ui, msg.splitlines(True))
    else:
      ui.warn( "ERROR: scm-hook failed with an unknown error\n" )
    ui.traceback()
  except ValueError:
    ui.warn( "scm-hook failed with an exception\n" )
    ui.traceback()
  return abort

def callback(ui, repo, hooktype, node=None):
  abort = True
  if node != None:
    if len(baseUrl) > 0:
      abort = callHookUrl(ui, repo, hooktype, node)
    else:
      ui.warn("ERROR: scm-manager hooks are disabled, please check your configuration and the scm-manager log for details\n")
      abort = False
  else:
    ui.warn("changeset node is not available")
  return abort

def preHook(ui, repo, hooktype, node=None, source=None, pending=None, **kwargs):
  # older mercurial versions
  if pending != None:
    pending()

  # newer mercurial version
  # we have to make in-memory changes visible to external process
  # this does not happen automatically, because mercurial treat our hooks as internal hooks
  # see hook.py at mercurial sources _exthook
  try:
    if repo is not None:
      tr = repo.currenttransaction()
      repo.dirstate.write(tr)
      if tr and not tr.writepending():
        ui.warn("no pending write transaction found")
  except AttributeError:
    ui.debug("mercurial does not support currenttransation")
    # do nothing

  return callback(ui, repo, hooktype, node)

def postHook(ui, repo, hooktype, node=None, source=None, pending=None, **kwargs):
  return callback(ui, repo, hooktype, node)

