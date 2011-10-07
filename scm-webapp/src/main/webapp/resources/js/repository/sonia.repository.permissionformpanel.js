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

Sonia.repository.PermissionFormPanel = Ext.extend(Sonia.repository.FormPanel, {

  permissionStore: null,
  
  addIcon: 'resources/images/add.gif',
  removeIcon: 'resources/images/delete.gif',
  
  titleText: 'Permissions',

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
      title: this.titleText,
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
          icon: this.addIcon,
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
          icon: this.removeIcon,
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

// register xtype
Ext.reg('repositoryPermissionsForm', Sonia.repository.PermissionFormPanel);
