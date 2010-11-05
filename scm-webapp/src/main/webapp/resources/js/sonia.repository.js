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

var repositoryTypeStore = new Ext.data.JsonStore({
  id: 1,
  fields: [ 'displayName', 'name' ]
});

function loadRepositoryTypes(state){
  repositoryTypeStore.loadData( state.repositoryTypes );
}

// register login callback
loginCallbacks.push( loadRepositoryTypes );

// register namespace
Ext.ns('Sonia.repository');


// RepositoryGrid
Sonia.repository.Grid = Ext.extend(Ext.grid.GridPanel, {

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

    var repositoryStore = new Sonia.rest.JsonStore({
      url: restUrl + 'repositories.json',
      root: 'repositories',
      fields: [ 'id', 'name', 'type', 'contact', 'description', 'creationDate', 'url' ],
      sortInfo: {
        field: 'name'
      }
    });

    var repositoryColModel = new Ext.grid.ColumnModel({
      columns: [
        {header: 'Name', sortable: true, dataIndex: 'name'},
        {header: 'Type', sortable: true, dataIndex: 'type', renderer: this.renderRepositoryType},
        {header: 'Contact', sortable: true, dataIndex: 'contact', scope: this, renderer: this.renderMailto},
        {header: 'Description', sortable: true, dataIndex: 'description'},
        {header: 'Creation date', sortable: true, dataIndex: 'creationDate'},
        {header: 'Url', sortable: true, dataIndex: 'url', scope: this, renderer: this.renderUrl}
      ]
    });

    var config = {
      store: repositoryStore,
      colModel: repositoryColModel,
      sm: selectionModel
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.repository.Grid.superclass.initComponent.apply(this, arguments);

    // load data
    if ( debug ){
      console.debug( 'load repository list' );
    }
    repositoryStore.load();
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
      if ( debug ){
        console.debug( selected.data.name + ' selected' );
      }
      var editPanel = Ext.getCmp('repositoryEditPanel');
      editPanel.removeAll();
      var panel = new Sonia.repository.FormPanel({
        item: selected.data,
        region: 'south',
        title: 'Repository Form',
        padding: 5,
        onUpdate: {
          fn: this.reload,
          scope: this
        },
        onCreate: {
          fn: this.reload,
          scope: this
        }
      });
      editPanel.add(panel);
      editPanel.doLayout();
    }
  },

  renderRepositoryType: function(repositoryType){
    return repositoryTypeStore.queryBy(function(rec){
      return rec.data.name == repositoryType;
    }).itemAt(0).data.displayName;
  },

  renderUrl: function(url){
    return String.format( this.urlTemplate, url );
  },

  renderMailto: function(mail){
    return String.format( this.mailtoTemplate, mail );
  }

  
});

// register xtype
Ext.reg('repositoryGrid', Sonia.repository.Grid);

// RepositoryFormPanel
Sonia.repository.FormPanel = Ext.extend(Ext.FormPanel,{

  item: null,
  onUpdate: null,
  onCreate: null,

  initComponent: function(){

    update = this.item != null;

    var config = {
      padding: 5,
      labelWidth: 100,
      defaults: {width: 240},
      autoScroll: true,
      defaultType: 'textfield',
      items:[
        {id: 'repositoryName', fieldLabel: 'Name', name: 'name', readOnly: update, allowBlank: false},
        {
         fieldLabel: 'Type',
         name: 'type',
         xtype: 'combo',
         readOnly: update,
         hiddenName : 'type',
         typeAhead: true,
         triggerAction: 'all',
         lazyRender: true,
         mode: 'local',
         editable: false,
         store: repositoryTypeStore,
         valueField: 'name',
         displayField: 'displayName',
         allowBlank: false
        },

        {fieldLabel: 'Contact', name: 'contact'},
        {fieldLabel: 'Description', name: 'description', xtype: 'textarea'}
      ],
      buttonAlign: 'center',
      buttons: [
        {text: 'Ok', scope: this, handler: this.submit},
        {text: 'Cancel', scope: this, handler: this.reset}
      ]
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.repository.FormPanel.superclass.initComponent.apply(this, arguments);

    if ( update ){
      this.loadData( this.item );
    }
  },

  loadData: function(item){
    this.item = item;
    var data = {success: true, data: item};
    this.getForm().loadRecord(data);
  },

  submit: function(){
    if ( debug ){
      console.debug( 'repository form submitted' );
    }
    var item = this.getForm().getFieldValues();
    if ( this.item != null ){
      this.update(item);
    } else {
      this.create(item);
    }
  },

  update: function(item){
    item = Ext.apply( this.item, item );
    if ( debug ){
      console.debug( 'update repository: ' + item.name );
    }
    var url = restUrl + 'repositories/' + item.id + '.json';
    Ext.Ajax.request({
      url: url,
      jsonData: item,
      method: 'PUT',
      scope: this,
      success: function(){
        if ( debug ){
          console.debug('update success');
        }
        this.execCallback(this.onUpdate, item);
      },
      failure: function(){
        alert( 'failure' );
      }
    });
  },

  create: function(item){
    if ( debug ){
      console.debug( 'create repository: ' + item.name );
    }
    var url = restUrl + 'repositories.json';
    Ext.Ajax.request({
      url: url,
      jsonData: item,
      method: 'POST',
      scope: this,
      success: function(){
        if ( debug ){
          console.debug('create success');
        }
        this.getForm().reset();
        this.execCallback(this.onCreate, item);
      },
      failure: function(){
        alert( 'failure' );
      }
    });
  },

  reset: function(){
    this.getForm().reset();
  },

  execCallback: function(obj, item){
    if ( Ext.isFunction( obj ) ){
      obj(item);
    } else if ( Ext.isObject( obj )){
      obj.fn.call( obj.scope, item );
    }
  }

});

// register xtype
Ext.reg('repositoryForm', Sonia.repository.FormPanel);

// RepositoryPanel
Sonia.repository.Panel = Ext.extend(Ext.Panel, {

  initComponent: function(){

    var config = {
      layout: 'border',
      hideMode: 'offsets',
      bodyCssClass: 'x-panel-mc',
      enableTabScroll: true,
      region:'center',
      autoScroll: true,
      tbar: [
        {xtype: 'tbbutton', text: 'Add', scope: this, handler: this.showAddForm},
        {xtype: 'tbbutton', text: 'Remove', scope: this, handler: this.remove}
      ],
      items: [{
          id: 'repositoryGrid',
          xtype: 'repositoryGrid',
          region: 'center'
        }, {
          id: 'repositoryEditPanel',
          layout: 'fit',
          items: [{
            //xtype: 'repositoryForm',
            region: 'south',
            title: 'Repository Form',
            xtype: 'panel',
            padding: 5,
            html: 'Add or select an Repository'
          }],
          height: 250,
          split: true,
          border: false,
          region: 'south'
        }
      ]
    }

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.repository.Panel.superclass.initComponent.apply(this, arguments);
  },

  remove: function(){
    var grid = Ext.getCmp('repositoryGrid');
    var selected = grid.getSelectionModel().getSelected();
    if ( selected ){
      var item = selected.data;
      var url = restUrl + 'repositories/' + item.id + '.json';
      
      Ext.MessageBox.show({
        title: 'Remove Repository',
        msg: 'Remove Repository "' + item.name + '"?',
        buttons: Ext.MessageBox.OKCANCEL,
        icon: Ext.MessageBox.QUESTION,
        fn: function(result){
          if ( result == 'ok' ){

            if ( debug ){
              console.debug( 'remove repository ' + item.name );
            }

            Ext.Ajax.request({
              url: url,
              method: 'DELETE',
              scope: this,
              success: function(){
                this.reload();
                this.resetPanel();
              },
              failure: function(){
                alert( 'failure' );
              }
            });
          }

        },
        scope: this
      });

    } else if ( debug ){
      console.debug( 'no repository selected' );
    }
  },

  resetPanel: function(){
    var editPanel = Ext.getCmp('repositoryEditPanel');
    editPanel.removeAll();
    editPanel.add({
      region: 'south',
      title: 'Repository Form',
      padding: 5,
      xtype: 'panel',
      html: 'Add or select an Repository'
    });
    editPanel.doLayout();
  },

  showAddForm: function(){
    var editPanel = Ext.getCmp('repositoryEditPanel');
    editPanel.removeAll();
    var panel = new Sonia.repository.FormPanel({
      region: 'south',
      title: 'Repository Form',
      padding: 5,
      onUpdate: {
        fn: this.reload,
        scope: this
      },
      onCreate: {
        fn: this.reload,
        scope: this
      }
    });
    editPanel.add(panel);
    editPanel.doLayout();
  },

  reload: function(){
    Ext.getCmp('repositoryGrid').reload();
  }

});

// register xtype
Ext.reg('repositoryPanel', Sonia.repository.Panel);
