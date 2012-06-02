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

#
# registration .hg/hgrc:
#
# [hooks]
# changegroup.scm = python:scmhooks.callback
#

import os, urllib, urllib2

baseUrl = os.environ['SCM_URL']
challenge = os.environ['SCM_CHALLENGE']
credentials = os.environ['SCM_CREDENTIALS']

def callHookUrl(ui, repo, hooktype, node):
  abort = True
  try:
    url = baseUrl + hooktype
    ui.debug( "send scm-hook to " + url + " and " + node + "\n" )
    data = urllib.urlencode({'node': node, 'challenge': challenge, 'credentials': credentials, 'repositoryPath': repo.root})
    conn = urllib2.urlopen(url, data);
    if conn.code >= 200 and conn.code < 300:
      ui.debug( "scm-hook " + hooktype + " success with status code " + str(conn.code) + "\n" )
      abort = False
    else:
      ui.warn( "ERROR: scm-hook failed with error code " + str(conn.code) + "\n" )
  except ValueError:
    ui.warn( "scm-hook failed with an exception\n" )
  return abort

def callback(ui, repo, hooktype, node=None, source=None, pending=None, **kwargs):
  if pending != None:
    pending()
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
