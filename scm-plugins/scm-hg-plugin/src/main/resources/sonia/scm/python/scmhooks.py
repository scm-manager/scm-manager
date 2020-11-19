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

import os, sys, json, socket

# read environment
port = os.environ['SCM_HOOK_PORT']
challenge = os.environ['SCM_CHALLENGE']
token = os.environ['SCM_BEARER_TOKEN']
repositoryId = os.environ['SCM_REPOSITORY_ID']
transactionId = os.environ['SCM_TRANSACTION_ID']

def print_messages(ui, messages):
  for message in messages:
    msg = "[SCM]"
    if message['severity'] == "ERROR":
      msg += " Error"
    msg += ": " + message['message'] + "\n"
    ui.warn(msg.encode('utf-8'))

def fire_hook(ui, repo, hooktype, node):
  abort = True
  ui.debug( b"send scm-hook for " + node + b"\n" )
  connection = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
  try:
    values = {'token': token, 'type': hooktype, 'repositoryId': repositoryId, 'transactionId': transactionId, 'challenge': challenge, 'node': node.decode('utf8') }

    connection.connect(("127.0.0.1", int(port)))
    connection.send(json.dumps(values).encode('utf-8'))
    connection.sendall(b'\0')

    received = []
    byte = connection.recv(1)
    while byte != b'\0':
      received.append(byte)
      byte = connection.recv(1)

    message = b''.join(received).decode('utf-8')
    response = json.loads(message)

    abort = response['abort']
    print_messages(ui, response['messages'])

  except ValueError:
    ui.warn( b"scm-hook failed with an exception\n" )
    ui.traceback()
  finally:
    connection.close()
  return abort

def callback(ui, repo, hooktype, node=None):
  abort = True
  if node != None:
    if len(port) > 0:
      abort = fire_hook(ui, repo, hooktype, node)
    else:
      ui.warn(b"ERROR: scm-manager hooks are disabled, please check your configuration and the scm-manager log for details\n")
  else:
    ui.warn(b"changeset node is not available")
  return abort

def pre_hook(ui, repo, hooktype, node=None, source=None, pending=None, **kwargs):
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

  return callback(ui, repo, "PRE_RECEIVE", node)

def post_hook(ui, repo, hooktype, node=None, source=None, pending=None, **kwargs):
  return callback(ui, repo, "POST_RECEIVE", node)

