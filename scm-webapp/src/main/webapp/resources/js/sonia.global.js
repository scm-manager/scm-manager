/**
 * Copyright (c) 2009, Sebastian Sdorra
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * http://bitbucket.org/sdorra/scm-manager
 * 
 */


var debug = true;
var state = null;

// functions called after login
var loginCallbacks = [];

// function called after logout
var logoutCallbacks = [];

var restUrl = "api/rest/";

function loadState(s){
  if ( debug ){
    console.debug( s );
  }
  state = s;
  // call login callback functions
  Ext.each(loginCallbacks, function(callback){
    if ( Ext.isFunction(callback) ){
      callback(state);
    }
  });
}

function logout(){
  Ext.Ajax.request({
    url: restUrl + 'authentication/logout.json',
    method: 'GET',
    success: function(){
      if ( debug ){
        console.debug('logout success');
      }
      // clear state
      state = null;
      // clear repository store
      repositoryTypeStore.removeAll();
      // remove all tabs
      Ext.getCmp('mainTabPanel').removeAll();
      // remove navigation items
      Ext.getCmp('navigationPanel').removeAll();
      // call logout callback functions
      Ext.each(logoutCallbacks, function(callback){
        if ( Ext.isFunction(callback) ){
          callback(state);
        }
      });
      // show login window
      var loginWin = new Sonia.login.Window();
      loginWin.show();
    },
    failure: function(){
      if ( debug ){
        console.debug('logout failed');
      }
      Ext.Msg.alert('Logout Failed!');
    }
  });
}
