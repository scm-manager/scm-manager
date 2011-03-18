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
  return admin || repository.permissions != null;
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

  colNameText: 'Name',
  colTypeText: 'Type',
  colContactText: 'Contact',
  colDescriptionText: 'Description',
  colCreationDateText: 'Creation date',
  colUrlText: 'Url',
  emptyText: 'No repository is configured',
  formTitleText: 'Repository Form',

  initComponent: function(){

    var repositoryStore = new Sonia.rest.JsonStore({
      id: 'repositoryStore',
      proxy: new Ext.data.HttpProxy({
        url: restUrl + 'repositories.json',
        disableCaching: false
      }),
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
        {id: 'name', header: this.colNameText, dataIndex: 'name'},
        {id: 'type', header: this.colTypeText, dataIndex: 'type', renderer: this.renderRepositoryType, width: 80},
        {id: 'contact', header: this.colContactText, dataIndex: 'contact', renderer: this.renderMailto},
        {id: 'description', header: this.colDescriptionText, dataIndex: 'description'},
        {id: 'creationDate', header: this.colCreationDateText, dataIndex: 'creationDate'},
        {id: 'Url', header: this.colUrlText, dataIndex: 'url', renderer: this.renderUrl, width: 250}
      ]
    });

    var config = {
      autoExpandColumn: 'description',
      store: repositoryStore,
      colModel: repositoryColModel,
      emptyText: this.emptyText
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
        title: this.formTitleText,
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

  colGroupPermissionText: 'Group Permission',
  colNameText: 'Name',
  colTypeText: 'Type',
  formTitleText: 'Settings',
  nameText: 'Name',
  typeText: 'Type',
  contactText: 'Contact',
  descriptionText: 'Description',
  publicText: 'Public',
  permissionText: 'Permission',
  errorTitleText: 'Error',
  updateErrorMsgText: 'Repository update failed',
  createErrorMsgText: 'Repository creation failed',

  // help
  nameHelpText: 'The name of the repository. This name would be part of the repository url.',
  typeHelpText: 'The type of the repository (e.g. Mercurial, Git or Subversion).',
  contactHelpText: 'An email address of the person who is in charge for this repository.',
  descriptionHelpText: 'A short description of the repository.',
  publicHelpText: 'A public repository which is readable by every person.',
  permissionHelpText: 'If the "Group Permission" box is checked, then the name represents the groupname otherwise the username.<br />\n\
  Type explenation:<br /><b>READ</b> = read permission<br /><b>WRITE</b> = read and write permission<br />\n\
  <b>OWNER</b> = read, write permissions and also the ability to manage the properties and permissions',

  permissionStore: null,

  initComponent: function(){

    update = this.item != null;

    var permissionStore = new Ext.data.JsonStore({
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
    this.permissionStore = permissionStore;

    var permissionColModel = new Ext.grid.ColumnModel({
      defaults: {
        sortable: true
      },
      columns: [{
         id: 'groupPermission',
         xtype: 'checkcolumn',
         header: this.colGroupPermissionText,
         dataIndex: 'groupPermission',
         width: 40
        },{
          id: 'name',
          header: this.colNameText,
          dataIndex: 'name',
          editable: true
        },{
          id: 'type',
          header: this.colTypeText,
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
        }],
      
        getCellEditor: function(colIndex, rowIndex) {
          if (colIndex == 1) {
            var store = null;
            var rec = permissionStore.getAt(rowIndex);
            if ( rec.data.groupPermission ){
              if (debug){
                console.debug( "using groupSearchStore" );
              }
              store = groupSearchStore;
            } else {
              if (debug){
                console.debug( "using userSearchStore" );
              }
              store = userSearchStore;
            }
            return new Ext.grid.GridEditor(new Ext.form.ComboBox({
              store: store,
              displayField: 'label',
              valueField: 'value',
              typeAhead: true,
              mode: 'remote',
              queryParam: 'query',
              hideTrigger: true,
              selectOnFocus:true,
              width: 250
            }));
          }
          return Ext.grid.ColumnModel.prototype.getCellEditor.call(this, colIndex, rowIndex);
        }
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
        title : this.formTitleText,
        collapsible : true,
        autoHeight : true,
        autoWidth: true,
        autoScroll: true,
        defaults: {width: 240},
        defaultType: 'textfield',
        buttonAlign: 'center',
        items: [{
            id: 'repositoryName',
            fieldLabel: this.nameText,
            name: 'name',
            readOnly: update,
            allowBlank: false,
            helpText: this.nameHelpText
          },{
           fieldLabel: this.typeText,
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
           allowBlank: false,
           helpText: this.typeHelpText
          },{
            fieldLabel: this.contactText,
            name: 'contact',
            vtype: 'email',
            helpText: this.contactHelpText
          },{
            fieldLabel: this.descriptionText,
            name: 'description',
            xtype: 'textarea',
            helpText: this.descriptionHelpText
          },{
            fieldLabel: this.publicText,
            name: 'public',
            xtype: 'checkbox',
            helpText: this.publicHelpText
          }
        ]
      },{
        id: 'permissionGrid',
        xtype: 'editorgrid',
        title: this.permissionsText,
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
          text: this.addText,
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
          text: this.removeText,
          scope: this,
          handler: function(){
            var grid = Ext.getCmp('permissionGrid');
            var selected = grid.getSelectionModel().getSelected();
            if ( selected ){
              this.permissionStore.remove(selected);
            }
          }
        }, '->',{
          id: 'permissionGridHelp',
          xtype: 'box',
          autoEl: {
            tag: 'img',
            src: 'resources/images/help.gif'
          }
        }]

      }]
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.repository.FormPanel.superclass.initComponent.apply(this, arguments);
  },

  afterRender: function(){
    // call super
    Sonia.repository.FormPanel.superclass.afterRender.apply(this, arguments);

    Ext.QuickTips.register({
      target: Ext.getCmp('permissionGridHelp'),
      title: '',
      text: this.permissionHelpText,
      enabled: true
    });
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
          title: this.errorTitleText,
          msg: this.updateErrorMsgText,
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
          title: this.errorTitleText,
          msg: this.createErrorMsgText,
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

  titleText: 'Repository Form',
  addText: 'Add',
  removeText: 'Remove',
  reloadText: 'Reload',
  emptyText: 'Add or select an Repository',
  removeTitleText: 'Remove Repository',
  removeMsgText: 'Remove Repository "{0}"?',
  errorTitleText: 'Error',
  errorMsgText: 'Repository deletion failed',

  initComponent: function(){

    var toolbar = [];
    if ( admin ){
      toolbar.push(
        {xtype: 'tbbutton', text: this.addText, scope: this, handler: this.showAddForm}
      );
    }
    toolbar.push(
      {xtype: 'tbbutton', id: 'repoRmButton', disabled: true, text: this.removeText, scope: this, handler: this.removeRepository},
      '-',
      {xtype: 'tbbutton', text: this.reloadText, scope: this, handler: this.reload}
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
            title: this.titleText,
            xtype: 'panel',
            padding: 5,
            html: this.emptyText
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
        title: this.removeTitleText,
        msg: String.format(this.removeMsgText, item.name),
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
                  title: this.errorTitleText,
                  msg: this.errorMsgText,
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
      title: this.titleText,
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
