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

Sonia.security.PermissionsPanel = Ext.extend(Ext.Panel, {

  addText: 'Add',
  removeText: 'Remove',
  reloadText: 'Reload',
  
  // icons
  addIcon: 'resources/images/add.png',
  removeIcon: 'resources/images/delete.png',
  reloadIcon: 'resources/images/reload.png',
  helpIcon: 'resources/images/help.png',


  //TODO i18n
  titleText: 'Permissions',
  
  permissionStore: null,
  baseUrl: null,
  
  initComponent: function(){

    this.permissionStore = new Sonia.rest.JsonStore({
      proxy: new Ext.data.HttpProxy({
        api: {
          read: restUrl + this.baseUrl + '.json'
        },
        disableCaching: false
      }),
      idProperty: 'id',
      fields: [ 'id', 'value'],
      listeners: {
        update: {
          fn: this.modifyOrAddPermission,
          scope: this
        },
        remove: {
          fn: this.removePermission,
          scope: this
        }
      }
    });
    
    var availablePermissionStore = new Ext.data.JsonStore({
      fields: ['display-name', 'description', 'value'],
      sortInfo: {
        field: 'display-name'
      }
    });
    availablePermissionStore.loadData(state.availablePermissions);
    
    var permissionColModel = new Ext.grid.ColumnModel({
      columns: [{
        id: 'value', 
        header: 'Permission', 
        dataIndex: 'value',
        renderer: this.renderPermission,
        editor: new Ext.form.ComboBox({
          store: availablePermissionStore,
          displayField: 'display-name',
          valueField: 'value',
          typeAhead: false,
          editable: false,
          triggerAction: 'all',
          mode: 'local',
          width: 250
        })
      }]
    });
    
    var selectionModel = new Ext.grid.RowSelectionModel({
      singleSelect: true
    });

    var config = {
      title: this.titleText,
      bodyCssClass: 'x-panel-mc',
      padding: 5,
      items: [{
        id: 'permissionGrid',
        xtype: 'editorgrid',
        clicksToEdit: 1,
        frame: true,
        width: '100%',
        autoHeight: true,
        autoScroll: false,
        colModel: permissionColModel,
        sm: selectionModel,
        store: this.permissionStore,
        viewConfig: {
          forceFit: true,
          markDirty: false
        },
        tbar: [{
          text: this.addText,
          scope: this,
          icon: this.addIcon,
          handler : function(){
            var Permission = this.permissionStore.recordType;
            var grid = Ext.getCmp('permissionGrid');
            grid.stopEditing();
            this.permissionStore.insert(0, new Permission());
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
        },'->',{
          id: 'permissionGridHelp',
          xtype: 'box',
          autoEl: {
            tag: 'img',
            src: this.helpIcon
          }
        }]
      }]
    };
    
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.security.PermissionsPanel.superclass.initComponent.apply(this, arguments);
  },
  
  getIdFromResponse: function(response){
    var id = null;
    var location = response.getResponseHeader('Location');
    if (location){
      var parts = location.split('/');
      id = parts[parts.length - 1];
    }
    return id;
  },
          
  modifyOrAddPermission: function(store, record){
    var id = record.get('id');
    if ( id ){
      this.modifyPermission(id, record);
    } else {
      this.addPermission(record);
    }
  },
  
  addPermission: function(record){
    Ext.Ajax.request({
      url: restUrl + this.baseUrl + '.json',
      method: 'POST',
      jsonData: record.data,
      scope: this,
      success: function(response){
        var id = this.getIdFromResponse(response);
        record.data.id = id;
      },
      failure: function(result){
      }
    });
  },
  
  modifyPermission: function(id, record){
    Ext.Ajax.request({
      url: restUrl + this.baseUrl + '/' + id + '.json',
      method: 'PUT',
      jsonData: record.data,
      scope: this,
      success: function(){
      },
      failure: function(result){
      }
    });
  },

  removePermission: function(store, record){
    Ext.Ajax.request({
      url: restUrl + this.baseUrl + '/' + record.get('id')  + '.json',
      method: 'DELETE',
      scope: this,
      success: function(){

      },
      failure: function(result){
      }
    });
  },
  
  renderPermission: function(value){
    var ap = state.availablePermissions;
    for (var i=0; i<ap.length; i++){
      if ( value === ap[i].value ){
        value = ap[i]['display-name'];
        break;
      }
    }
    return value;
  }

});

// register xtype
Ext.reg('permissionsPanel', Sonia.security.PermissionsPanel);