#
# Copyright (c) 2020 - present Cloudogu GmbH
#
# This program is free software: you can redistribute it and/or modify it under
# the terms of the GNU Affero General Public License as published by the Free
# Software Foundation, version 3.
#
# This program is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
# details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program. If not, see https://www.gnu.org/licenses/.
#

#
# registration .hg/hgrc:
#
# [hooks]
# changegroup.scm = python:scmhooks.callback
#

import os, sys, json, socket, struct

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

def read_bytes(connection, length):
  received = bytearray()
  while len(received) < length:
    buffer = connection.recv(length - len(received))
    received = received + buffer
  return received

def read_int(connection):
  data = read_bytes(connection, 4)
  return struct.unpack('>i', bytearray(data))[0]

def fire_hook(ui, repo, hooktype, node):
  abort = True
  ui.debug( b"send scm-hook for " + node + b"\n" )
  connection = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
  try:
    values = {'token': token, 'type': hooktype, 'repositoryId': repositoryId, 'transactionId': transactionId, 'challenge': challenge, 'node': node.decode('utf8') }

    connection.connect(("127.0.0.1", int(port)))

    data = json.dumps(values).encode('utf-8')
    connection.send(struct.pack('>i', len(data)))
    connection.sendall(data)

    length = read_int(connection)
    if length > 8192:
      ui.warn( b"scm-hook received message which exceeds the limit of 8192\n" )
      return True

    d = read_bytes(connection, length)
    response = json.loads(d.decode("utf-8"))

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
  # this does not happen automatically, because mercurial treat our hooks as internal hook
  # see hook.py at mercurial sources _exthook
  try:
    if repo is not None:
      tr = repo.currenttransaction()
      repo.dirstate.write(tr)
      if tr and not tr.writepending():
        ui.warn(b"no pending write transaction found")
  except AttributeError:
    ui.debug(b"mercurial does not support currenttransaction")
    # do nothing

  return callback(ui, repo, "PRE_RECEIVE", node)

def post_hook(ui, repo, hooktype, node=None, source=None, pending=None, **kwargs):
  return callback(ui, repo, "POST_RECEIVE", node)
