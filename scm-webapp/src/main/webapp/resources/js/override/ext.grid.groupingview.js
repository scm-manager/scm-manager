/*
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
Ext.grid.GroupingView.prototype.initTemplatesExt = Ext.grid.GroupingView.prototype.initTemplates;
Ext.grid.GroupingView.prototype.toggleGroupExt = Ext.grid.GroupingView.prototype.toggleGroup;

Ext.override(Ext.grid.GroupingView,{
  
  storedState: null,
  idPrefix: '{grid.el.id}',
  
  initTemplates : function(){
    this.initTemplatesExt();
    if (this.storedState){
      this.state = this.storedState;
    }
  },
  
  toggleGroup : function(group, expanded){
    this.toggleGroupExt(group, expanded);
    this.grid.fireEvent('toggleGroup', group, expanded);
  },
  
  getState: function(){
    return this.state;
  },
  
  applyState: function(state){
    this.storedState = state;
  },
  
  getPrefix: function(field){
    var prefix;
    if ( this.idPrefix === '{grid.id}' ){
      prefix = this.grid.getId();
    } else if (this.idPrefix === '{grid.el.id}') {
      prefix = this.grid.getGridEl().id;
    } else {
      prefix = this.idPrefix;
    }
    prefix += '-gp-' + field + '-';
    return prefix;
  }
  
});