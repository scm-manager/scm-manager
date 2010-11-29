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

// Grid

Sonia.rest.Grid = Ext.extend(Ext.grid.GridPanel, {

  urlTemplate: '<a href="{0}" target="_blank">{0}</a>',
  mailtoTemplate: '<a href="mailto: {0}">{0}</a>',
  checkboxTemplate: '<input type="checkbox" disabled="true" {0}/>',

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
    if (debug){
      console.debug( item );
    }
  },

  renderUrl: function(url){
    return String.format( this.urlTemplate, url );
  },

  renderMailto: function(mail){
    return String.format( this.mailtoTemplate, mail );
  },

  renderCheckbox: function(value){
    var param = "";
    if ( value ){
      param = "checked='checked' ";
    }
    return String.format( this.checkboxTemplate, param );
  }

});

// FormPanel

Sonia.rest.FormPanel = Ext.extend(Ext.FormPanel,{

  item: null,
  onUpdate: null,
  onCreate: null,

  initComponent: function(){
    var config = {
      padding: 5,
      labelWidth: 100,
      defaults: {width: 240},
      autoScroll: true,
      monitorValid: true,
      defaultType: 'textfield',
      buttonAlign: 'center',
      buttons: [
        {text: 'Ok', formBind: true, scope: this, handler: this.submit},
        {text: 'Cancel', scope: this, handler: this.reset}
      ]
    }

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.rest.FormPanel.superclass.initComponent.apply(this, arguments);

    if ( this.item != null ){
      this.loadData(this.item);
    }
  },

  loadData: function(item){
    this.item = item;
    var data = {success: true, data: item};
    this.getForm().loadRecord(data);
  },

  submit: function(){
    if ( debug ){
      console.debug( 'form submitted' );
    }
    var item = this.getForm().getFieldValues();
    if ( this.item != null ){
      this.update(item);
    } else {
      this.create(item);
    }
  },
  
  reset: function(){
    if ( debug ){
      console.debug( 'reset form' );
    }
    this.getForm().reset();
  },

  execCallback: function(obj, item){
    if ( Ext.isFunction( obj ) ){
      obj(item);
    } else if ( Ext.isObject( obj )){
      obj.fn.call( obj.scope, item );
    }
  },

  update: function(item){
    if ( debug ){
      console.debug( 'update item: ' );
      console.debug( item );
    }
  },

  create: function(item){
    if (debug){
      console.debug( 'create item: ' );
      console.debug( item );
    }
  }

});
