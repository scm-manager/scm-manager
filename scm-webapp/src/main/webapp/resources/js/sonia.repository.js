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
Sonia.repository.Grid = Ext.extend(Sonia.rest.Grid, {
  
  initComponent: function(){

    var repositoryStore = new Sonia.rest.JsonStore({
      url: restUrl + 'repositories.json',
      root: 'repositories',
      fields: [ 'id', 'name', 'type', 'contact', 'description', 'creationDate', 'url' ],
      sortInfo: {
        field: 'name'
      }
    });

    var repositoryColModel = new Ext.grid.ColumnModel({
      defaults: {
        sortable: true,
        scope: this,
        width: 125
      },
      columns: [
        {id: 'name', header: 'Name', dataIndex: 'name'},
        {id: 'type', header: 'Type', dataIndex: 'type', renderer: this.renderRepositoryType, width: 80},
        {id: 'contact', header: 'Contact', dataIndex: 'contact', renderer: this.renderMailto},
        {id: 'description', header: 'Description', dataIndex: 'description'},
        {id: 'creationDate', header: 'Creation date', dataIndex: 'creationDate'},
        {id: 'Url', header: 'Url', dataIndex: 'url', renderer: this.renderUrl, width: 250}
      ]
    });

    var config = {
      autoExpandColumn: 'description',
      store: repositoryStore,
      colModel: repositoryColModel
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.repository.Grid.superclass.initComponent.apply(this, arguments);
  },

  selectItem: function(item){
    if ( debug ){
      console.debug( item.name + ' selected' );
    }
    var editPanel = Ext.getCmp('repositoryEditPanel');
    editPanel.removeAll();
    var panel = new Sonia.repository.FormPanel({
      item: item,
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

  renderRepositoryType: function(repositoryType){
    return repositoryTypeStore.queryBy(function(rec){
      return rec.data.name == repositoryType;
    }).itemAt(0).data.displayName;
  }
  
});

// register xtype
Ext.reg('repositoryGrid', Sonia.repository.Grid);

// RepositoryFormPanel
Sonia.repository.FormPanel = Ext.extend(Sonia.rest.FormPanel,{

  initComponent: function(){

    update = this.item != null;

    var config = {
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

        {fieldLabel: 'Contact', name: 'contact', vtype: 'email'},
        {fieldLabel: 'Description', name: 'description', xtype: 'textarea'}
      ]
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.repository.FormPanel.superclass.initComponent.apply(this, arguments);
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
        {xtype: 'tbbutton', text: 'Remove', scope: this, handler: this.removeRepository},
        '-',
        {xtype: 'tbbutton', text: 'Reload', scope: this, handler: this.reload}
      ],
      items: [{
          id: 'repositoryGrid',
          xtype: 'repositoryGrid',
          region: 'center'
        }, {
          id: 'repositoryEditPanel',
          layout: 'fit',
          items: [{
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

  removeRepository: function(){
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
