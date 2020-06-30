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

import os, sys

client = None

# compatibility layer between python 2 and 3 urllib implementations
if sys.version_info[0] < 3:
  import urllib, urllib2
  # create type alias for url error
  URLError = urllib2.URLError

  class Python2Client:
    def post(self, url, values):
        data = urllib.urlencode(values)
        # open url but ignore proxy settings
        proxy_handler = urllib2.ProxyHandler({})
        opener = urllib2.build_opener(proxy_handler)
        req = urllib2.Request(url, data)
        req.add_header("X-XSRF-Token", xsrf)
        return opener.open(req)

  client = Python2Client()
else:
  import urllib.parse, urllib.request, urllib.error
  # create type alias for url error
  URLError = urllib.error.URLError

  class Python3Client:
    def post(self, url, values):
        data = urllib.parse.urlencode(values)
        # open url but ignore proxy settings
        proxy_handler = urllib.request.ProxyHandler({})
        opener = urllib.request.build_opener(proxy_handler)
        req = urllib.request.Request(url, data.encode())
        req.add_header("X-XSRF-Token", xsrf)
        return opener.open(req)

  client = Python3Client()

# read environment
baseUrl = os.environ['SCM_URL']
challenge = os.environ['SCM_CHALLENGE']
token = os.environ['SCM_BEARER_TOKEN']
xsrf = os.environ['SCM_XSRF']
repositoryId = os.environ['SCM_REPOSITORY_ID']

def printMessages(ui, msgs):
  for raw in msgs:
    line = raw
    if hasattr(line, "encode"):
      line = line.encode()
    if line.startswith(b"_e") or line.startswith(b"_n"):
      line = line[2:]
    ui.warn(b'%s\n' % line.rstrip())

def callHookUrl(ui, repo, hooktype, node):
  abort = True
  try:
    url = baseUrl + hooktype.decode("utf-8")
    ui.debug( b"send scm-hook to " + url.encode() + b" and " + node + b"\n" )
    values = {'node': node.decode("utf-8"), 'challenge': challenge, 'token': token, 'repositoryPath': repo.root, 'repositoryId': repositoryId}
    conn = client.post(url, values)
    if 200 <= conn.code < 300:
      ui.debug( b"scm-hook " + hooktype + b" success with status code " + str(conn.code).encode() + b"\n" )
      printMessages(ui, conn)
      abort = False
    else:
      ui.warn( b"ERROR: scm-hook failed with error code " + str(conn.code).encode() + b"\n" )
  except URLError as e:
    msg = None
    # some URLErrors have no read method
    if hasattr(e, "read"):
      msg = e.read()
    elif hasattr(e, "code"):
      msg = "scm-hook failed with error code " + e.code + "\n"
    else:
      msg = str(e)
    if len(msg) > 0:
      printMessages(ui, msg.splitlines(True))
    else:
      ui.warn( b"ERROR: scm-hook failed with an unknown error\n" )
    ui.traceback()
  except ValueError:
    ui.warn( b"scm-hook failed with an exception\n" )
    ui.traceback()
  return abort

def callback(ui, repo, hooktype, node=None):
  abort = True
  if node != None:
    if len(baseUrl) > 0:
      abort = callHookUrl(ui, repo, hooktype, node)
    else:
      ui.warn(b"ERROR: scm-manager hooks are disabled, please check your configuration and the scm-manager log for details\n")
      abort = False
  else:
    ui.warn(b"changeset node is not available")
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
        ui.warn(b"no pending write transaction found")
  except AttributeError:
    ui.debug(b"mercurial does not support currenttransation")
    # do nothing

  return callback(ui, repo, hooktype, node)

def postHook(ui, repo, hooktype, node=None, source=None, pending=None, **kwargs):
  return callback(ui, repo, hooktype, node)
