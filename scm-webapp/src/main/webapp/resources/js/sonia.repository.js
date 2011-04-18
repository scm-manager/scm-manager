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

Sonia.repository.setEditPanel = function(panels){
  var editPanel = Ext.getCmp('repositoryEditPanel');
  editPanel.removeAll();
  Ext.each(panels, function(panel){
    editPanel.add(panel);
  });
  editPanel.setActiveTab(0);
  editPanel.doLayout();
}

/**
 * panels
 *
 * TODO: missing i18n
 */

Sonia.repository.DefaultPanel = {
  region: 'south',
  title: 'Repository Form',
  padding: 5,
  xtype: 'panel',
  bodyCssClass: 'x-panel-mc',
  html: 'Add or select an Repository'
}

Sonia.repository.NoPermissionPanel = {
  region: 'south',
  title: 'Repository Form',
  padding: 5,
  xtype: 'panel',
  bodyCssClass: 'x-panel-mc',
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
        {id: 'creationDate', header: this.colCreationDateText, dataIndex: 'creationDate', renderer: Ext.util.Format.formatTimestamp},
        {id: 'Url', header: this.colUrlText, dataIndex: 'url', renderer: this.renderUrl, width: 250}
      ]
    });

    var config = {
      autoExpandColumn: 'description',
      store: repositoryStore,
      colModel: repositoryColModel,
      emptyText: this.emptyText,
      listeners: {
        fallBelowMinHeight: {
          fn: this.onFallBelowMinHeight,
          scope: this
        }
      }
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.repository.Grid.superclass.initComponent.apply(this, arguments);
  },

  onFallBelowMinHeight: function(height, minHeight){
    var p = Ext.getCmp('repositoryEditPanel');
    this.setHeight(minHeight);
    var epHeight = p.getHeight();
    p.setHeight(epHeight - (minHeight - height));
    // rerender
    this.doLayout();
    p.doLayout();
    this.ownerCt.doLayout();
  },

  selectItem: function(item){
    if ( debug ){
      console.debug( item.name + ' selected' );
    }

    var panels = [{
      item: item,
      xtype: 'repositoryInfoPanel'
    }];
    
    if ( Sonia.repository.isOwner(item) ){
      Ext.getCmp('repoRmButton').setDisabled(false);
      panels.push({
        item: item,
        xtype: 'repositoryPropertiesForm',
        onUpdate: {
          fn: this.reload,
          scope: this
        },
        onCreate: {
          fn: this.reload,
          scope: this
        }
      },{
        item: item,
        xtype: 'repositoryPermissionsForm',
        listeners: {
          updated: {
            fn: this.reload,
            scope: this
          },
          created: {
            fn: this.reload,
            scope: this
          }
        }
      });
    } else {
      Ext.getCmp('repoRmButton').setDisabled(true);
    }

    Sonia.repository.setEditPanel(panels);
  },

  renderRepositoryType: function(repositoryType){
    return repositoryTypeStore.queryBy(function(rec){
      return rec.data.name == repositoryType;
    }).itemAt(0).data.displayName;
  }
  
});

// register xtype
Ext.reg('repositoryGrid', Sonia.repository.Grid);

Sonia.repository.InfoPanel = Ext.extend(Ext.Panel, {

  linkTemplate: '<a target="_blank" href="{0}">{0}</a>',
  actionLinkTemplate: '<a style="cursor: pointer;">{0}</a>',

  initComponent: function(){
    var config = {
      title: this.item.name,
      padding: 5,
      bodyCssClass: 'x-panel-mc',
      layout: 'table',
      layoutConfig: {
        columns: 2,
        tableAttrs: {
          style: 'width: 80%;'
        }
      },
      defaults: {
        style: 'font-size: 12px'
      },
      // TODO i18n
      items: [{
        xtype: 'label',
        text: 'Name: '
      },{
        xtype: 'box',
        html: this.item.name
      },{
        xtype: 'label',
        text: 'Type: '
      },{
        xtype: 'box',
        html: this.getRepositoryTypeText(this.item.type)
      },{
        xtype: 'label',
        text: 'Url: '
      },{
        xtype: 'box',
        html: String.format(this.linkTemplate, this.item.url)
      },{
        xtype: 'box',
        height: 10,
        colspan: 2
      },{
        xtype: 'link',
        colspan: 2,
        text: 'ChangesetViewer',
        listeners: {
          click: {
            fn: this.openChangesetViewer,
            scope: this
          }
        }
      }]
    }
    
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.repository.InfoPanel.superclass.initComponent.apply(this, arguments);
  },

  getRepositoryTypeText: function(t){
    var text = null;
    for ( var i=0; i<state.repositoryTypes.length; i++ ){
      var type = state.repositoryTypes[i];
      if ( type.name == t ){
        text = type.displayName + " (" + t + ")";
        break;
      }
    }
    return text;
  },

  openChangesetViewer: function(){
    var changesetViewer = {
      id: this.item.id + '-changesetViewer',
      title: 'ChangesetViewer ' + this.item.name,
      repository: this.item,
      xtype: 'repositoryChangesetViewerPanel',
      closable: true,
      autoScroll: true
    }
    main.addTab(changesetViewer);
  }
  
});

// register xtype
Ext.reg('repositoryInfoPanel', Sonia.repository.InfoPanel);

// RepositoryFormPanel
Sonia.repository.FormPanel = Ext.extend(Sonia.rest.FormPanel,{

  colGroupPermissionText: 'Is Group',
  colNameText: 'Name',
  colTypeText: 'Permissions',
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
  nameHelpText: 'The name of the repository. This name will be part of the repository url.',
  typeHelpText: 'The type of the repository (e.g. Mercurial, Git or Subversion).',
  contactHelpText: 'Email address of the person who is responsible for this repository.',
  descriptionHelpText: 'A short description of the repository.',
  publicHelpText: 'Public repository, readable by everyone.',
  permissionHelpText: 'Manage permissions for a specific user or group.<br />\n\
  Permissions explenation:<br /><b>READ</b> = read<br /><b>WRITE</b> = read and write<br />\n\
  <b>OWNER</b> = read, write and also the ability to manage the properties and permissions',

  initComponent: function(){
    this.addEvents('preCreate', 'created', 'preUpdate', 'updated', 'updateFailed', 'creationFailed');
    Sonia.repository.FormPanel.superclass.initComponent.apply(this, arguments);
  },

  update: function(item){
    item = Ext.apply( this.item, item );
    if ( debug ){
      console.debug( 'update repository: ' + item.name );
    }
    var url = restUrl + 'repositories/' + item.id + '.json';
    var el = this.el;
    var tid = setTimeout( function(){el.mask('Loading ...');}, 100);

    this.fireEvent('preUpdate', item);

    Ext.Ajax.request({
      url: url,
      jsonData: item,
      method: 'PUT',
      scope: this,
      success: function(){
        if ( debug ){
          console.debug('update success');
        }
        this.fireEvent('updated', item);
        clearTimeout(tid);
        el.unmask();
        this.execCallback(this.onUpdate, item);
      },
      failure: function(){
        this.fireEvent('updateFailed', item);
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

    this.fireEvent('preCreate', item);

    Ext.Ajax.request({
      url: url,
      jsonData: item,
      method: 'POST',
      scope: this,
      success: function(){
        if ( debug ){
          console.debug('create success');
        }
        this.fireEvent('created', item);
        this.getForm().reset();
        clearTimeout(tid);
        el.unmask();
        this.execCallback(this.onCreate, item);
      },
      failure: function(){
        this.fireEvent('creationFailed', item);
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


Sonia.repository.PropertiesFormPanel = Ext.extend(Sonia.repository.FormPanel, {

  initComponent: function(){
    var update = this.item != null;

    var config = {
      title: this.formTitleText,
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
      }]
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.repository.PropertiesFormPanel.superclass.initComponent.apply(this, arguments);
  }

});

// register xtype
Ext.reg('repositoryPropertiesForm', Sonia.repository.PropertiesFormPanel);


Sonia.repository.PermissionFormPanel = Ext.extend(Sonia.repository.FormPanel, {

  permissionStore: null,

  initComponent: function(){
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
        },{
         id: 'groupPermission',
         xtype: 'checkcolumn',
         header: this.colGroupPermissionText,
         dataIndex: 'groupPermission',
         width: 40
        }],

        getCellEditor: function(colIndex, rowIndex) {
          if (colIndex == 0) {
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

    if ( this.item != null ){
      if ( this.item.permissions == null ){
        this.item.permissions = [];
      }
      this.permissionStore.loadData( this.item );
    }

    var selectionModel = new Ext.grid.RowSelectionModel({
      singleSelect: true
    });

    var config = {
      // TODO i18n
      title: 'Permissions',
      listeners: {
        updated: this.clearModifications,
        preUpdate: {
          fn: this.updatePermissions,
          scope: this
        }
      },
      items:[{
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
    Sonia.repository.PermissionFormPanel.superclass.initComponent.apply(this, arguments);
  },
  
  afterRender: function(){
    // call super
    Sonia.repository.PermissionFormPanel.superclass.afterRender.apply(this, arguments);

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
  }

});

Ext.reg('repositoryPermissionsForm', Sonia.repository.PermissionFormPanel);

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
          xtype: 'tabpanel',
          activeTab: 0,
          height: 250,
          split: true,
          border: true,
          region: 'south',
          items: [{
            bodyCssClass: 'x-panel-mc',
            title: this.titleText,
            padding: 5,
            html: this.emptyText
          }]
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
    Sonia.repository.setEditPanel([{
      xtype: 'repositoryPropertiesForm',
      listeners: {
        updated: {
          fn: this.reload,
          scope: this
        },
        created: {
          fn: this.reload,
          scope: this
        }
      }
    }]);
  },

  reload: function(){
    Ext.getCmp('repositoryGrid').reload();
  }

});

// register xtype
Ext.reg('repositoryPanel', Sonia.repository.Panel);


// changeset viewer

Sonia.repository.ChangesetViewerGrid = Ext.extend(Ext.grid.GridPanel, {

  repository: null,
  changesetMetadataTemplate: '<div class="changeset-description">{description:htmlEncode}</div>\
                              <div class="changeset-author">{author:htmlEncode}</div>\
                              <div class="changeset-date">{date:formatTimestamp}</div>',
  modificationsTemplate: 'A: {0}, M: {1}, D: {2}',
  idsTemplate: 'Commit: {0}',
  tagsAndBranchesTemplate: '<div class="changeset-tags">{0}</div>\
                            <div class="changeset-branches">{1}</div>',
  

  initComponent: function(){

    var changesetColModel = new Ext.grid.ColumnModel({
      defaults: {
        sortable: false
      },
      columns: [{
        id: 'metadata',
        xtype: 'templatecolumn',
        dataIndex: 'author',
        tpl: this.changesetMetadataTemplate
      },{
        id: 'tagsAndBranches',
        renderer: this.renderTagsAndBranches,
        scope: this
      },{
        id: 'modifications',
        dataIndex: 'modifications',
        renderer: this.renderModifications,
        scope: this,
        width: 100
      },{
        id: 'ids',
        dataIndex: 'id',
        renderer: this.renderIds,
        scope: this,
        width: 180
      }]
    });

    var config = {
      header: false,
      autoScroll: true,
      autoExpandColumn: 'metadata',
      height: '100%',
      hideHeaders: true,
      colModel: changesetColModel
    }
    
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.repository.ChangesetViewerGrid.superclass.initComponent.apply(this, arguments);
  },

  renderTagsAndBranches: function(value, p, record){
    var tags = this.getLabeledValue("Tags", record.data.tags);
    var branches = this.getLabeledValue("Branches", record.data.branches);
    return String.format(this.tagsAndBranchesTemplate, tags, branches);
  },

  getLabeledValue: function(label, array){
    var result = '';
    if ( array != null && array.length > 0 ){
      result = label + ': ' + Sonia.util.getStringFromArray(array);
    }
    return result;
  },

  renderIds: function(value){
    return String.format(this.idsTemplate, value);
  },

  renderModifications: function(value){
    var added = 0;
    var modified = 0;
    var removed = 0;
    if ( Ext.isDefined(value) ){
      if ( Ext.isDefined(value.added) ){
        added = value.added.length;
      }
      if ( Ext.isDefined(value.modified) ){
        modified = value.modified.length;
      }
      if ( Ext.isDefined(value.removed) ){
        removed = value.removed.length;
      }
    }
    return String.format(this.modificationsTemplate, added, modified, removed);
  }

});

// register xtype
Ext.reg('repositoryChangesetViewerGrid', Sonia.repository.ChangesetViewerGrid);

Sonia.repository.ChangesetViewerPanel = Ext.extend(Ext.Panel, {

  repository: null,
  pageSize: 20,

  initComponent: function(){

    var changesetStore = new Ext.data.JsonStore({
      id: 'changesetStore',
      proxy: new Ext.data.HttpProxy({
        url: restUrl + 'repositories/' + this.repository.id  + '/changesets.json',
        method: 'GET'
      }),
      fields: ['id', 'date', 'author', 'description', 'modifications', 'tags', 'branches'],
      root: 'changesets',
      idProperty: 'id',
      totalProperty: 'total',
      autoLoad: false,
      autoDestroy: true
    });

    changesetStore.load({
      params: {
        start:0,
        limit: this.pageSize
      }
    });

    var config = {
      items: [{
        xtype: 'repositoryChangesetViewerGrid',
        repository: this.repository,
        store: changesetStore
      }, {
        xtype: 'paging',
        store: changesetStore,
        displayInfo: true,
        pageSize: this.pageSize,
        prependButtons: true
      }]
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.repository.ChangesetViewerPanel.superclass.initComponent.apply(this, arguments);
  }

});

// register xtype
Ext.reg('repositoryChangesetViewerPanel', Sonia.repository.ChangesetViewerPanel);