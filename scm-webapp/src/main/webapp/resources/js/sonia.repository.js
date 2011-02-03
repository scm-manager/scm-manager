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
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER S AND CONTRIBUTORS "AS IS"
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

// functions

Sonia.repository.isOwner = function(repository){
  return repository.permissions != null;
}

Sonia.repository.setEditPanel = function(panel){
  var editPanel = Ext.getCmp('repositoryEditPanel');
  editPanel.removeAll();
  editPanel.add(panel);
  editPanel.doLayout();
}

// panels

Sonia.repository.DefaultPanel = {
  region: 'south',
  title: 'Repository Form',
  padding: 5,
  xtype: 'panel',
  html: 'Add or select an Repository'
}

Sonia.repository.NoPermissionPanel = {
  region: 'south',
  title: 'Repository Form',
  padding: 5,
  xtype: 'panel',
  html: 'No permission to modify this repository'
}

// components

// RepositoryGrid
Sonia.repository.Grid = Ext.extend(Sonia.rest.Grid, {
  
  initComponent: function(){

    var repositoryStore = new Sonia.rest.JsonStore({
      url: restUrl + 'repositories.json',
      fields: [ 'id', 'name', 'type', 'contact', 'description', 'creationDate', 'url', 'public', 'permissions' ],
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
      colModel: repositoryColModel,
      emptyText: 'No repository is configured'
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.repository.Grid.superclass.initComponent.apply(this, arguments);
  },

  selectItem: function(item){
    if ( debug ){
      console.debug( item.name + ' selected' );
    }

    var panel = null;
    
    if ( Sonia.repository.isOwner(item) ){
      Ext.getCmp('repoRmButton').setDisabled(false);
      panel = new Sonia.repository.FormPanel({
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
    } else {
      Ext.getCmp('repoRmButton').setDisabled(true);
      panel = Sonia.repository.NoPermissionPanel;
    }
    Sonia.repository.setEditPanel(panel);
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

  permissionStore: null,

  initComponent: function(){

    update = this.item != null;

    this.permissionStore = new Ext.data.JsonStore({
      root: 'permissions',
      fields: [ 
        {name: 'name'},
        {name: 'type'},
        {name: 'groupPermission', type: 'boolean'}
      ],
      sortInfo: {
        field: 'name'
      }
    });

    var permissionColModel = new Ext.grid.ColumnModel({
      defaults: {
        sortable: true
      },
      columns: [{
         id: 'groupPermission',
         xtype: 'checkcolumn',
         header: 'Group Permission',
         dataIndex: 'groupPermission',
         width: 40
        },{
          id: 'name',
          header: 'Name',
          dataIndex: 'name',
          editor: new Ext.form.TextField({
            allowBlank: false
          })
        },{
          id: 'type',
          header: 'Type',
          dataIndex: 'type',
          width: 80,
          editor: new Ext.form.ComboBox({
            valueField: 'type',
            displayField: 'type',
            typeAhead: false,
            editable: false,
            triggerAction: 'all',
            mode: 'local',
            store: new Ext.data.SimpleStore({
              fields: ['type'],
              data: [
                ['READ'],
                ['WRITE'],
                ['OWNER']
              ]
            })
          })
        }]
    });

    if ( update ){
      if ( this.item.permissions == null ){
        this.item.permissions = [];
      }
      this.permissionStore.loadData( this.item );
    }

    var selectionModel = new Ext.grid.RowSelectionModel({
      singleSelect: true
    });

    var config = {
      items:[{
        xtype : 'fieldset',
        checkboxToggle : false,
        title : 'Settings',
        collapsible : true,
        autoHeight : true,
        autoWidth: true,
        autoScroll: true,
        defaults: {width: 240},
        defaultType: 'textfield',
        buttonAlign: 'center',
        items: [
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
          {fieldLabel: 'Description', name: 'description', xtype: 'textarea'},
          {fieldLabel: 'Public', name: 'public', xtype: 'checkbox'}
        ]
      },{
        id: 'permissionGrid',
        xtype: 'editorgrid',
        title: 'Permissions',
        clicksToEdit: 1,
        autoExpandColumn: 'name',
        frame: true,
        width: '100%',
        autoHeight: true,
        autoScroll: false,
        colModel: permissionColModel,
        sm: selectionModel,
        store: this.permissionStore,
        viewConfig: {
          forceFit:true
        },
        tbar: [{
          text: 'Add',
          scope: this,
          handler : function(){
            var Permission = this.permissionStore.recordType;
            var p = new Permission({
              type: 'READ'
            });
            var grid = Ext.getCmp('permissionGrid');
            grid.stopEditing();
            this.permissionStore.insert(0, p);
            grid.startEditing(0, 0);
          }
        },{
          text: 'Remove',
          scope: this,
          handler: function(){
            var grid = Ext.getCmp('permissionGrid');
            var selected = grid.getSelectionModel().getSelected();
            if ( selected ){
              this.permissionStore.remove(selected);
            }
          }
        }]

      }]
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.repository.FormPanel.superclass.initComponent.apply(this, arguments);
  },

  updatePermissions: function(item){
    var permissions = [];
    this.permissionStore.data.each(function(record){
      permissions.push( record.data );
    })
    item.permissions = permissions;
  },

  clearModifications: function(){
    Ext.getCmp('permissionGrid').getStore().commitChanges();
  },

  update: function(item){
    item = Ext.apply( this.item, item );
    if ( debug ){
      console.debug( 'update repository: ' + item.name );
    }
    var url = restUrl + 'repositories/' + item.id + '.json';
    var el = this.el;
    var tid = setTimeout( function(){el.mask('Loading ...');}, 100);

    this.updatePermissions(item);

    Ext.Ajax.request({
      url: url,
      jsonData: item,
      method: 'PUT',
      scope: this,
      success: function(){
        if ( debug ){
          console.debug('update success');
        }
        this.clearModifications();
        clearTimeout(tid);
        el.unmask();
        this.execCallback(this.onUpdate, item);
      },
      failure: function(){
        clearTimeout(tid);
        el.unmask();
        Ext.MessageBox.show({
          title: 'Error',
          msg: 'Repository update failed',
          buttons: Ext.MessageBox.OK,
          icon:Ext.MessageBox.ERROR
        });
      }
    });
  },

  create: function(item){
    if ( debug ){
      console.debug( 'create repository: ' + item.name );
    }
    var url = restUrl + 'repositories.json';
    var el = this.el;
    var tid = setTimeout( function(){el.mask('Loading ...');}, 100);
    
    this.updatePermissions(item);

    Ext.Ajax.request({
      url: url,
      jsonData: item,
      method: 'POST',
      scope: this,
      success: function(){
        if ( debug ){
          console.debug('create success');
        }
        this.permissionStore.removeAll();
        this.getForm().reset();
        clearTimeout(tid);
        el.unmask();
        this.execCallback(this.onCreate, item);
      },
      failure: function(){
        clearTimeout(tid);
        el.unmask();
        Ext.MessageBox.show({
          title: 'Error',
          msg: 'Repository creation failed',
          buttons: Ext.MessageBox.OK,
          icon:Ext.MessageBox.ERROR
        });
      }
    });
  },

  cancel: function(){
    if ( debug ){
      console.debug( 'cancel form' );
    }
    Sonia.repository.setEditPanel( Sonia.repository.DefaultPanel );
  }

});

// register xtype
Ext.reg('repositoryForm', Sonia.repository.FormPanel);

// RepositoryPanel
Sonia.repository.Panel = Ext.extend(Ext.Panel, {

  initComponent: function(){

    var toolbar = [];
    if ( admin ){
      toolbar.push(
        {xtype: 'tbbutton', text: 'Add', scope: this, handler: this.showAddForm}
      );
    }
    toolbar.push(
      {xtype: 'tbbutton', id: 'repoRmButton', disabled: true, text: 'Remove', scope: this, handler: this.removeRepository},
      '-',
      {xtype: 'tbbutton', text: 'Reload', scope: this, handler: this.reload}
    );

    var config = {
      layout: 'border',
      hideMode: 'offsets',
      bodyCssClass: 'x-panel-mc',
      enableTabScroll: true,
      region:'center',
      autoScroll: true,
      tbar: toolbar,
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
                Ext.MessageBox.show({
                  title: 'Error',
                  msg: 'Repository deletion failed',
                  buttons: Ext.MessageBox.OK,
                  icon:Ext.MessageBox.ERROR
                });
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
    Sonia.repository.setEditPanel(Sonia.repository.DefaultPanel);
  },

  showAddForm: function(){
    Ext.getCmp('repoRmButton').setDisabled(true);
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
    Sonia.repository.setEditPanel(panel);
  },

  reload: function(){
    Ext.getCmp('repositoryGrid').reload();
  }

});

// register xtype
Ext.reg('repositoryPanel', Sonia.repository.Panel);
