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

Sonia.group.setEditPanel = function(panel){
  var editPanel = Ext.getCmp('groupEditPanel');
  editPanel.removeAll();
  editPanel.add(panel);
  editPanel.doLayout();
}

// GroupGrid
Sonia.group.Grid = Ext.extend(Sonia.rest.Grid, {

  initComponent: function(){
    var groupStore = new Sonia.rest.JsonStore({
      url: restUrl + 'groups.json',
      fields: [ 'name', 'members', 'creationDate', 'type'],
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
        {id: 'name', header: 'Name', dataIndex: 'name'},
        {id: 'members', header: 'Members', dataIndex: 'members', renderer: this.renderMembers},
        {id: 'creationDate', header: 'Creation date', dataIndex: 'creationDate'},
        {id: 'type', header: 'Type', dataIndex: 'type', width: 80}
      ]
    });

    var config = {
      autoExpandColumn: 'members',
      store: groupStore,
      colModel: groupColModel,
      emptyText: 'No group is configured'
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.group.Grid.superclass.initComponent.apply(this, arguments);
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

    Ext.getCmp('removeButton').setDisabled(false);
    var panel = new Sonia.group.FormPanel({
      item: group,
      region: 'south',
      title: 'Group Form',
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
    Sonia.group.setEditPanel(panel);
  }

});

// register xtype
Ext.reg('groupGrid', Sonia.group.Grid);

// GroupFormPanel
Sonia.group.FormPanel = Ext.extend(Sonia.rest.FormPanel,{

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
        header: 'Member',
        dataIndex: 'member',
        editor: new Ext.form.TextField({
          allowBlank: false
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
      if ( debug ){
        console.debug( data );
      }
      this.memberStore.loadData( data );
    }

    var items = [{
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
      items: [{
        fieldLabel: 'Name',
        name: 'name',
        allowBlank: false,
        readOnly: this.item != null
      }]
    },{
      id: 'memberGrid',
      xtype: 'editorgrid',
      title: 'Members',
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
        text: 'Add',
        scope: this,
        handler : function(){
          var Member = this.memberStore.recordType;
          var grid = Ext.getCmp('memberGrid');
          grid.stopEditing();
          this.memberStore.insert(0, new Member());
          grid.startEditing(0, 0);
        }
      },{
        text: 'Remove',
        scope: this,
        handler: function(){
          var grid = Ext.getCmp('memberGrid');
          var selected = grid.getSelectionModel().getSelected();
          if ( selected ){
            this.memberStore.remove(selected);
          }
        }
      }]
    }];

    Ext.apply(this, Ext.apply(this.initialConfig, {items: items}));
    Sonia.group.FormPanel.superclass.initComponent.apply(this, arguments);
  }

});

// register xtype
Ext.reg('groupFormPanel', Sonia.group.FormPanel);


// RepositoryPanel
Sonia.group.Panel = Ext.extend(Ext.Panel, {

  initComponent: function(){
    var config = {
      layout: 'border',
      hideMode: 'offsets',
      bodyCssClass: 'x-panel-mc',
      enableTabScroll: true,
      region:'center',
      autoScroll: true,
      tbar: [
        {xtype: 'tbbutton', text: 'Add', scope: this},
        {xtype: 'tbbutton', id: 'removeButton', disabled: true, text: 'Remove', scope: this}
      ],
      items: [{
          id: 'groupGrid',
          xtype: 'groupGrid',
          region: 'center'
        }, {
          id: 'groupEditPanel',
          layout: 'fit',
          items: [{
            region: 'south',
            title: 'Group Form',
            xtype: 'panel',
            padding: 5,
            html: 'Add or select a Group'
          }],
          height: 250,
          split: true,
          border: false,
          region: 'south'
        }
      ]
    }

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.group.Panel.superclass.initComponent.apply(this, arguments);
  }

});

// register xtype
Ext.reg('groupPanel', Sonia.group.Panel);
