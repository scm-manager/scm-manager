/* *
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

// register namespace
Ext.ns('Sonia.group');

Sonia.group.setEditPanel = function(panels){
  var editPanel = Ext.getCmp('groupEditPanel');
  editPanel.removeAll();
  Ext.each(panels, function(panel){
    editPanel.add(panel);
  });
  editPanel.setActiveTab(0);
  editPanel.doLayout();
}

/**
 * panels
 */
Sonia.group.DefaultPanel = {
  region: 'south',
  title: 'Group Form',
  xtype: 'panel',
  bodyCssClass: 'x-panel-mc',
  padding: 5,
  html: 'Add or select a Group'
}

// GroupGrid
Sonia.group.Grid = Ext.extend(Sonia.rest.Grid, {

  colNameText: 'Name',
  colDescriptionText: 'Description',
  colMembersText: 'Members',
  colCreationDateText: 'Creation date',
  colTypeText: 'Type',
  emptyGroupStoreText: 'No group is configured',
  groupFormTitleText: 'Group Form',

  initComponent: function(){
    var groupStore = new Sonia.rest.JsonStore({
      proxy: new Ext.data.HttpProxy({
        url: restUrl + 'groups.json',
        disableCaching: false
      }),
      fields: [ 'name', 'members', 'description', 'creationDate', 'type'],
      sortInfo: {
        field: 'name'
      }
    });

    var groupColModel = new Ext.grid.ColumnModel({
      defaults: {
        sortable: true,
        scope: this,
        width: 125
      },
      columns: [
        {id: 'name', header: this.colNameText, dataIndex: 'name'},
        {id: 'description', header: this.colDescriptionText, dataIndex: 'description', width: 300 },
        {id: 'members', header: this.colMembersText, dataIndex: 'members', renderer: this.renderMembers},
        {id: 'creationDate', header: this.colCreationDateText, dataIndex: 'creationDate', renderer: Ext.util.Format.formatTimestamp},
        {id: 'type', header: this.colTypeText, dataIndex: 'type', width: 80}
      ]
    });

    var config = {
      autoExpandColumn: 'members',
      store: groupStore,
      colModel: groupColModel,
      emptyText: this.emptyGroupStoreText,
      listeners: {
        fallBelowMinHeight: {
          fn: this.onFallBelowMinHeight,
          scope: this
        }
      }
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.group.Grid.superclass.initComponent.apply(this, arguments);
  },


  onFallBelowMinHeight: function(height, minHeight){
    var p = Ext.getCmp('groupEditPanel');
    this.setHeight(minHeight);
    var epHeight = p.getHeight();
    p.setHeight(epHeight - (minHeight - height));
    // rerender
    this.doLayout();
    p.doLayout();
    this.ownerCt.doLayout();
  },


  renderMembers: function(members){
    var out = '';
    if ( members != null ){
      var s = members.length;
      for ( var i=0; i<s; i++ ){
        out += members[i];
        if ( (i+1)<s ){
          out += ', ';
        }
      }
    }
    return out;
  },

  selectItem: function(group){
    if ( debug ){
      console.debug( group.name + ' selected' );
    }

    Ext.getCmp('groupRmButton').setDisabled(false);
    Sonia.group.setEditPanel([{
      item: group,
      xtype: 'groupPropertiesForm',
      onUpdate: {
        fn: this.reload,
        scope: this
      },
      onCreate: {
        fn: this.reload,
        scope: this
      }
    },{
      item: group,
      xtype: 'groupPermissionsForm',
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
  }

});

// register xtype
Ext.reg('groupGrid', Sonia.group.Grid);

// GroupFormPanel
Sonia.group.FormPanel = Ext.extend(Sonia.rest.FormPanel,{

  colMemberText: 'Member',
  titleText: 'Settings',
  nameText: 'Name',
  descriptionText: 'Description',
  membersText: 'Members',
  errorTitleText: 'Error',
  updateErrorMsgText: 'Group update failed',
  createErrorMsgText: 'Group creation failed',

  // help
  nameHelpText: 'Unique name of the group.',
  descriptionHelpText: 'A short description of the group.',
  membersHelpText: 'Usernames of the group members.',

  initComponent: function(){
    this.addEvents('preCreate', 'created', 'preUpdate', 'updated', 'updateFailed', 'creationFailed');
    Sonia.repository.FormPanel.superclass.initComponent.apply(this, arguments);
  },

  update: function(group){
    if ( debug ){
      console.debug( 'update group ' + group.name );
    }
    group = Ext.apply( this.item, group );

    // this.updateMembers(group);
    this.fireEvent('preUpdate', group);

    var url = restUrl + 'groups/' + group.name + '.json';
    var el = this.el;
    var tid = setTimeout( function(){el.mask('Loading ...');}, 100);

    Ext.Ajax.request({
      url: url,
      jsonData: group,
      method: 'PUT',
      scope: this,
      success: function(){
        if ( debug ){
          console.debug('update success');
        }
        this.fireEvent('updated', group);
        clearTimeout(tid);
        el.unmask();
        this.execCallback(this.onUpdate, group);
      },
      failure: function(result){
        this.fireEvent('updateFailed', group);
        clearTimeout(tid);
        el.unmask();
        main.handleFailure(
          result.status, 
          this.errorTitleText, 
          this.updateErrorMsgText
        );
      }
    });
  },

  create: function(item){
    if ( debug ){
      console.debug( 'create group: ' + item.name );
    }
    item.type = 'xml';

    var url = restUrl + 'groups.json';
    var el = this.el;
    var tid = setTimeout( function(){el.mask('Loading ...');}, 100);
    
    this.fireEvent('preCreate', item);

    // this.updateMembers(item);

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
        // this.memberStore.removeAll();
        this.getForm().reset();
        clearTimeout(tid);
        el.unmask();
        this.execCallback(this.onCreate, item);
      },
      failure: function(result){
        this.fireEvent('creationFailed', item);
        clearTimeout(tid);
        el.unmask();
        main.handleFailure(
          result.status, 
          this.errorTitleText, 
          this.createErrorMsgText
        );
      }
    });
  },

  cancel: function(){
    if ( debug ){
      console.debug( 'cancel form' );
    }
    Sonia.group.setEditPanel( Sonia.group.DefaultPanel );
  }

});

// register xtype
Ext.reg('groupFormPanel', Sonia.group.FormPanel);

// group properties

Sonia.group.PropertiesFormPanel = Ext.extend(Sonia.group.FormPanel, {
  
  initComponent: function(){
    var config = {
      title: this.titleText,
      items: [{
        fieldLabel: this.nameText,
        name: 'name',
        allowBlank: false,
        readOnly: this.item != null,
        helpText: this.nameHelpText
      },{
        fieldLabel: this.descriptionText,
        name: 'description',
        xtype: 'textarea',
        helpText: this.descriptionHelpText
      }]
    }
    
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.group.PropertiesFormPanel.superclass.initComponent.apply(this, arguments);
  }
  
});

// register xtype
Ext.reg('groupPropertiesForm', Sonia.group.PropertiesFormPanel);

// group permissions

Sonia.group.PermissionFormPanel = Ext.extend(Sonia.group.FormPanel, {
    
  memberStore: null,
  
  initComponent: function(){  
    this.memberStore = new Ext.data.SimpleStore({
      fields: ['member'],
      sortInfo: {
        field: 'member'
      }
    });

    var memberColModel = new Ext.grid.ColumnModel({
      defaults: {
        sortable: true
      },
      columns: [{
        id: 'member',
        header: this.colMemberText,
        dataIndex: 'member',
        editor: new Ext.form.ComboBox({
          store: userSearchStore,
          displayField: 'label',
          valueField: 'value',
          typeAhead: true,
          mode: 'remote',
          queryParam: 'query',  //contents of the field sent to server.
          hideTrigger: true,    //hide trigger so it doesn't look like a combobox.
          selectOnFocus:true,
          width: 250
        })
      }]
    });

    var selectionModel = new Ext.grid.RowSelectionModel({
      singleSelect: true
    });

    if ( this.item != null ){
      var data = [];
      if ( this.item.members != null ){
        for ( var i=0; i<this.item.members.length; i++ ){
          var a = [];
          a.push( this.item.members[i] );
          data.push(a);
        }
      }
      this.memberStore.loadData( data );
    }
    
    var config = { 
      title: this.membersText,
      listeners: {
        updated: this.clearModifications,
        preUpdate: {
          fn: this.updateMembers,
          scope: this
        }
      },
      items: [{
        id: 'memberGrid',
        xtype: 'editorgrid',
        clicksToEdit: 1,
        frame: true,
        width: '100%',
        autoHeight: true,
        autoScroll: false,
        colModel: memberColModel,
        sm: selectionModel,
        store: this.memberStore,
        viewConfig: {
          forceFit:true
        },
        tbar: [{
          text: this.addText,
          scope: this,
          handler : function(){
            var Member = this.memberStore.recordType;
            var grid = Ext.getCmp('memberGrid');
            grid.stopEditing();
            this.memberStore.insert(0, new Member());
            grid.startEditing(0, 0);
          }
        },{
          text: this.removeText,
          scope: this,
          handler: function(){
            var grid = Ext.getCmp('memberGrid');
            var selected = grid.getSelectionModel().getSelected();
            if ( selected ){
              this.memberStore.remove(selected);
            }
          }
        },'->',{
          id: 'memberGridHelp',
          xtype: 'box',
          autoEl: {
            tag: 'img',
            src: 'resources/images/help.gif'
          }
        }]
      }]
    };
    
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.group.PermissionFormPanel.superclass.initComponent.apply(this, arguments);
  },
  
  afterRender: function(){
    // call super
    Sonia.group.FormPanel.superclass.afterRender.apply(this, arguments);
    
    Ext.QuickTips.register({
      target: Ext.getCmp('memberGridHelp'),
      title: '',
      text: this.membersHelpText,
      enabled: true
    });
  },

  updateMembers: function(item){
    var members = [];
    this.memberStore.data.each(function(record){
      members.push( record.data.member );
    })
    item.members = members;
  },

  clearModifications: function(){
    Ext.getCmp('memberGrid').getStore().commitChanges();
  }
  
});

Ext.reg('groupPermissionsForm', Sonia.group.PermissionFormPanel);


// RepositoryPanel
Sonia.group.Panel = Ext.extend(Ext.Panel, {

  addText: 'Add',
  removeText: 'Remove',
  reloadText: 'Reload',
  titleText: 'Group Form',
  emptyText: 'Add or select a Group',
  removeTitleText: 'Remove Group',
  removeMsgText: 'Remove Group "{0}"?',
  errorTitleText: 'Error',
  errorMsgText: 'Group deletion failed',

  initComponent: function(){
    var config = {
      layout: 'border',
      hideMode: 'offsets',
      enableTabScroll: true,
      region:'center',
      autoScroll: true,
      tbar: [
        {xtype: 'tbbutton', text: this.addText, scope: this, handler: this.showAddForm},
        {xtype: 'tbbutton', id: 'groupRmButton', disabled: true, text: this.removeText, scope: this, handler: this.removeGroup},
        '-',
        {xtype: 'tbbutton', text: this.reloadText, scope: this, handler: this.reload}
      ],
      items: [{
          id: 'groupGrid',
          xtype: 'groupGrid',
          region: 'center'
        }, {
          id: 'groupEditPanel',
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
    Sonia.group.Panel.superclass.initComponent.apply(this, arguments);
  },

  removeGroup: function(){
    var grid = Ext.getCmp('groupGrid');
    var selected = grid.getSelectionModel().getSelected();
    if ( selected ){
      var item = selected.data;
      var url = restUrl + 'groups/' + item.name + '.json';

      Ext.MessageBox.show({
        title: this.removeTitleText,
        msg: String.format( this.removeMsgText, item.name ),
        buttons: Ext.MessageBox.OKCANCEL,
        icon: Ext.MessageBox.QUESTION,
        fn: function(result){
          if ( result == 'ok' ){

            if ( debug ){
              console.debug( 'remove group ' + item.name );
            }

            Ext.Ajax.request({
              url: url,
              method: 'DELETE',
              scope: this,
              success: function(){
                this.reload();
                this.resetPanel();
              },
              failure: function(result){
                main.handleFailure(
                  result.status, 
                  this.errorTitleText, 
                  this.errorMsgText
                );
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

  showAddForm: function(){
    Ext.getCmp('groupRmButton').setDisabled(true);
    Sonia.group.setEditPanel({
      xtype: 'groupPropertiesForm',
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
  },

  resetPanel: function(){
    Sonia.group.setEditPanel(Sonia.group.DefaultPanel);
  },

  reload: function(){
    Ext.getCmp('groupGrid').reload();
  }

});

// register xtype
Ext.reg('groupPanel', Sonia.group.Panel);
