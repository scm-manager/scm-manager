/**
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

Ext.ns("Sonia.rest");

Sonia.rest.JsonStore = Ext.extend( Ext.data.JsonStore, {

  constructor: function(config) {
    var baseConfig = {
      autoLoad: false,
      listeners: {
        // fix jersey empty array problem
        exception: {
          fn: function(proxy, type, action, options, response, arg){
            var status = response.status;
            if ( status == 200 && action == 'read' && response.responseText == 'null' ){
              if ( debug ){
                console.debug( 'empty array, clear whole store' );
              }
              this.removeAll();
            } else {
              alert( action + "(" + status + "): " + response.responseText );
            }
          },
          scope: this
        }
      }
    };
    Sonia.rest.JsonStore.superclass.constructor.call(this, Ext.apply(config, baseConfig));
  }

});

Sonia.rest.Grid = Ext.extend(Ext.grid.GridPanel, {

  urlTemplate: '<a href="{0}" target="_blank">{0}</a>',
  mailtoTemplate: '<a href="mailto: {0}">{0}</a>',

  initComponent: function(){

    var selectionModel = new Ext.grid.RowSelectionModel({
      singleSelect: true
    });

    selectionModel.on({
      selectionchange: {
        scope: this,
        fn: this.selectionChanged
      }
    });

    var config = {
      loadMask: true,
      sm: selectionModel
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.rest.Grid.superclass.initComponent.apply(this, arguments);

    // load store
    if ( debug ){
      console.debug( 'load store' );
    }
    this.store.load();
  },

  reload: function(){
    if ( debug ){
      console.debug('reload store');
    }
    this.store.load();
  },

  selectionChanged: function(sm){
    var selected = sm.getSelected();
    if ( selected ){
      this.selectItem( selected.data );
    }
  },

  selectItem: function(item){
    
  },

  renderUrl: function(url){
    return String.format( this.urlTemplate, url );
  },

  renderMailto: function(mail){
    return String.format( this.mailtoTemplate, mail );
  }


});