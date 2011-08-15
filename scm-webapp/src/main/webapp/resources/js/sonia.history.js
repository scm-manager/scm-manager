/* *
 * Copyright (c) 2010, Sebastian Sdorra
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

Ext.ns('Sonia');

Sonia.History = {
 
  historyElements: [],
  recentlyAdded: [],
  
  add: function(token){
    if ( debug ){
      console.debug('add history element ' + token);
    }
    this.recentlyAdded.push(token);
    Ext.History.add(token, true);
  },
  
  append: function(item){
    return this.appendWithDepth(item, 1);
  },
  
  appendWithDepth: function(item, depth){
    var token = Ext.History.getToken();
    if ( token ){
      var tokenSuffix = '';
      if (Ext.isArray(item)){
        for (var i=0; i<item.length; i++){
          tokenSuffix += item[i];
          if ( (i+1)<item.length ){
            tokenSuffix += '|';
          }
        }
      } else {
        tokenSuffix = item;
      }
      
      var parts = token.split('|');
      var newToken = '';
      for (var j=0; j<depth; j++){
        newToken += parts[j] + '|';
      }
      newToken += tokenSuffix;
      this.add(newToken);
      token = newToken;
    }
    return token;
  },
  
  register: function(id, fn, scope){
    if (scope){
      this.historyElements[id] = {
        'fn': fn,
        'scope': scope
      };
    } else {
      this.historyElements[id] = fn;
    }
    
  },
  
  handleChange: function(id, params){
    var el = this.historyElements[id];
    if (el){
      if (debug){
        console.debug('handle history event for ' + id + ' with "' + params + '"');
      }
      if (Ext.isFunction(el) ){
        el(params);
      } else {
        el.fn.call(el.scope, params);
      }
    } else if (debug){
      console.debug('could not find history element for ' + id);
    }
  }

};


Ext.History.on('change', function(token){
  if(token){
    var found = false;
    for ( var i=0; i<Sonia.History.recentlyAdded.length; i++ ){
      if (Sonia.History.recentlyAdded[i] == token){
        found = true;
        Sonia.History.recentlyAdded.splice(i);
        break;
      }
    }
    
    if (!found){  
      var parts = token.split('|');
      var id = parts[0];
      Sonia.History.handleChange(id, parts.splice(1));
    }
    
  } else if (debug) {
    console.debug('history token is empty');
  }
});