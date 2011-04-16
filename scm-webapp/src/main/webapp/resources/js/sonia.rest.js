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

  errorTitleText: 'Error',
  errorMsgText: 'Could not load items. Server returned status: {0}',

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
              Ext.MessageBox.show({
                title: this.errorTitleText,
                msg: String.format( this.errorMsgText, status ),
                buttons: Ext.MessageBox.OK,
                icon:Ext.MessageBox.ERROR
              });
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
  emptyText: 'No items available',
  minHeight: 150,

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
      minHeight: this.minHeight,
      loadMask: true,
      sm: selectionModel,
      viewConfig: {
        deferEmptyText: false,
        emptyText: this.emptyText
      }
    };

    this.addEvents('fallBelowMinHeight');

    Ext.EventManager.onWindowResize(this.resize, this);

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.rest.Grid.superclass.initComponent.apply(this, arguments);

    // load store
    if ( debug ){
      console.debug( 'load store' );
    }
    this.store.load();
  },

  resize: function(){
    var h = this.getHeight();
    if (debug){
      console.debug('' + h + ' < ' + this.minHeight + " = " + (h < this.minHeight));
    }
    if ( h < this.minHeight ){
      if ( debug ){
        console.debug( 'fire event fallBelowMinHeight' );
      }
      this.fireEvent('fallBelowMinHeight', h, this.minHeight);
    }
  },

  onDestroy: function(){
    Ext.EventManager.removeResizeListener(this.resize, this);
    Sonia.rest.Grid.superclass.onDestroy.apply(this, arguments);
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
    var result = '';
    if ( url != null ){
      result = String.format( this.urlTemplate, url );
    }
    return result;
  },

  renderMailto: function(mail){
    var result = '';
    if ( mail != null ){
      result = String.format( this.mailtoTemplate, mail );
    }
    return result;
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

  okText: 'Ok',
  cancelText: 'Cancel',
  addText: 'Add',
  removeText: 'Remove',

  item: null,
  onUpdate: null,
  onCreate: null,

  initComponent: function(){
    var config = {
      bodyCssClass: 'x-panel-mc',
      padding: 5,
      labelWidth: 100,
      defaults: {width: 240},
      autoScroll: true,
      monitorValid: true,
      defaultType: 'textfield',
      buttonAlign: 'center',
      footerCfg: {
        cls: 'x-panel-mc'
      },
      buttons: [
        {text: this.okText, formBind: true, scope: this, handler: this.submit},
        {text: this.cancelText, scope: this, handler: this.cancel}
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
  
  cancel: function(){
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
