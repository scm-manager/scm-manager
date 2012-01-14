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

Sonia.group.MemberFormPanel = Ext.extend(Sonia.group.FormPanel, {
    
  memberStore: null,
  addIcon: 'resources/images/add.png',
  removeIcon: 'resources/images/delete.png',
  
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
          icon: this.addIcon,
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
          icon: this.removeIcon,
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
    Sonia.group.MemberFormPanel.superclass.initComponent.apply(this, arguments);
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

Ext.reg('groupMemberForm', Sonia.group.MemberFormPanel);
