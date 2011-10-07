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
      id: 'name',
      fields: [ 'name', 'members', 'description', 'creationDate', 'type', 'properties'],
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
    
    Sonia.History.append(group.name);

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
      xtype: 'groupMemberForm',
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
