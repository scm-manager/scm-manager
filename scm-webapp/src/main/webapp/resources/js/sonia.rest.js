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

Sonia.rest.EditForm = Ext.extend(Ext.form.FormPanel, {

  title: 'Edit REST',
  data: null,
  focusField: null,
  
  initComponent: function(){

    var config = {
      labelWidth: 100,
      autoHeight: true,
      frame: true,
      title: this.title,
      defaultType:'textfield',
      monitorValid: true,
      defaults: {width: 240},
      listeners: {
        afterrender: {
          fn: function(){
            if ( this.focusField != null && this.data == null ){
              Ext.getCmp(this.focusField).focus(true, 500);
            }
          },
          scope: this
        }
      },
      buttonAlign: 'center',
      buttons:[
        {text: 'Ok', formBind: true, scope: this, handler: this.submit},
        {text: 'Cancel', scope: this, handler: this.cancel}
      ]
    };

    this.addEvents('submit', 'cancel');

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.rest.EditForm.superclass.initComponent.apply(this, arguments);

    if ( this.data != null ){
      this.load(this.data);
    }
  },

  load: function(item){
    var data = {success: true, data: item};
    this.getForm().loadRecord( data );
  },

  submit: function(){
    var form = this.getForm();
    var item = this.getItem( form );
    this.fireEvent('submit', item);
  },

  getItem: function(form){
    return form.getFieldValues();
  },

  cancel: function(){
    this.fireEvent('cancel', false);
  }

});

Ext.reg('restEditForm', Sonia.rest.EditForm);

Sonia.rest.Grid = Ext.extend(Ext.grid.GridPanel, {

  restAddUrl: null,
  restEditUrlPattern: null,
  restRemoveUrlPattern: null,
  idField: null,
  nameField: null,
  searchField: null,
  editForm: null,
  editWindowWidth: 300,

  initComponent: function(){

    var restSelModel = new Ext.grid.RowSelectionModel({
      singleSelect: true
    });

    var restToolbar = new Ext.Toolbar({
      items: [
        {xtype: 'tbbutton', text: 'Add', scope: this, handler: this.showAddForm},
        {xtype: 'tbbutton', text: 'Edit', scope: this, handler: this.showEditWindow},
        {xtype: 'tbbutton', text: 'Remove', scope: this, handler: this.removeItem},
        {xtype: 'tbbutton', text: 'Reload', scope: this, handler: this.reload},
        {xtype: 'tbseparator'},
        {xtype: 'label', text: 'Search: '},
        {xtype: 'textfield', listeners: {
          specialkey: {
            fn: function(field, e){
              if (e.getKey() == e.ENTER) {
                this.search(field.getValue());
              }
            },
            scope: this
          }
        }}
      ]
    });

    var config = {
      region: 'center',
      autoHeight: true,
      selModel: restSelModel,
      tbar: restToolbar,
      viewConfig: {
        forceFit: true
      },
      loadMask: true,
      listeners: {
        celldblclick: this.showEditWindow
      }
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.rest.Grid.superclass.initComponent.apply(this, arguments);
    
    // load data
    this.store.load();
  },

  showPanel: function(pn){
    var panel = Ext.getCmp('southpanel');
    panel.removeAll();
    panel.add( pn );
    panel.doLayout();
  },

  resetPanel: function(){
    this.showPanel({
      xtype: 'panel',
      html: 'select or add'
    });
  },

  showAddForm: function(){
    var form = this.editForm;
    form.data = null;
    form.listeners = {
      submit: {
        fn: function(item){

          var store = this.store;

          if ( debug ){
            console.debug( 'add item ' + item[this.nameField] );
          }
          Ext.Ajax.request({
            url: this.restAddUrl,
            jsonData: item,
            method: 'POST',
            scope: this,
            success: function(){
              store.reload();
              this.resetPanel();
            },
            failure: function(){
              alert( 'failure' );
            }
          });

        },
        scope: this
      },
      cancel: {
        fn: this.resetPanel,
        scope: this
      }
    };

    this.showPanel( form );
  },

  showEditWindow: function(){
    if ( this.selModel.hasSelection() ){
      var data = this.selModel.getSelected().data;

      var form = this.editForm;
      form.data = data;
      form.listeners = {
        submit: {
          fn: function(item){

            item = Ext.apply(data, item);

            var store = this.store;
            var id = data[this.idField];
            var url = String.format(this.restEditUrlPattern, id);

            if ( debug ){
              console.debug( 'update item ' + id );
            }

            Ext.Ajax.request({
              url: url,
              jsonData: item,
              method: 'PUT',
              scope: this,
              success: function(){
                store.reload();
                this.resetPanel();
              },
              failure: function(){
                alert( 'failure' );
              }
            });

          },
          scope: this
        },
        cancel: {
          fn: this.resetPanel,
          scope: this
        }
      }

      this.showPanel( form );
    }
  },

  removeItem: function(){
    if ( this.selModel.hasSelection() ){
      var selected = this.selModel.getSelected();
      var id = selected.data[this.idField]
      var store = this.store;
      var url = String.format( this.restRemoveUrlPattern, id );
      var name = this.nameField != null ? selected.data[this.nameField] : id;

      Ext.MessageBox.show({
        title: 'Remove Item',
        msg: 'Remove Item "' + name + '"?',
        buttons: Ext.MessageBox.OKCANCEL,
        icon: Ext.MessageBox.QUESTION,
        fn: function(result){
          if ( result == 'ok' ){

            if ( debug ){
              console.debug( 'remove item ' + id );
            }
            
            Ext.Ajax.request({
              url: url,
              method: 'DELETE',
              success: function(){
                store.reload();
              },
              failure: function(){
                alert( 'failure' );
              }
            });
          }
          
        }
      });

    }
  },

  reload: function(){
    this.store.reload();
  },

  search: function(value){
    if ( this.searchField != null ){
      this.store.filter(this.searchField, new RegExp('.*' + value + '.*'));
    }
  }

});

Ext.reg('restGrid', Sonia.rest.Grid);

Sonia.rest.RestPanel = Ext.extend(Ext.Panel,{

  grid: null,
  title: null,

  initComponent: function(){
    var config = {
      title: this.title,
      layout: 'border',
      border: false,
      items:[
        this.grid,{
          id: 'southpanel',
          xtype: 'panel',
          region: 'south',
          autoScroll: true,
          split: true,
          frame: true,
          height: 200,
          items:[{
            xtype: 'panel',
            html: 'Select or add'
          }]
        }
      ]
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.rest.RestPanel.superclass.initComponent.apply(this, arguments);
  }

});

Ext.reg('restPanel', Sonia.rest.RestPanel);
