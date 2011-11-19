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

Sonia.group.Panel = Ext.extend(Sonia.rest.Panel, {

  titleText: 'Group Form',
  emptyText: 'Add or select a Group',
  removeTitleText: 'Remove Group',
  removeMsgText: 'Remove Group "{0}"?',
  errorTitleText: 'Error',
  errorMsgText: 'Group deletion failed',
  
  // grid
  groupGrid: null,

  initComponent: function(){
    var config = {
      tbar: [
        {xtype: 'tbbutton', text: this.addText, icon: this.addIcon, scope: this, handler: this.showAddForm},
        {xtype: 'tbbutton', id: 'groupRmButton', disabled: true, text: this.removeText, icon: this.removeIcon, scope: this, handler: this.removeGroup},
        '-',
        {xtype: 'tbbutton', text: this.reloadText, icon: this.reloadIcon, scope: this, handler: this.reload}
      ],
      items: [{
          id: 'groupGrid',
          xtype: 'groupGrid',
          region: 'center',
          parentPanel: this
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
  
  getGrid: function(){
    if (!this.groupGrid){
      if (debug){
        console.debug('could not find group grid, fetch by id');
      }
      this.groupGrid = Ext.getCmp('groupGrid');
    }
    return this.groupGrid;
  },
  
  updateHistory: function(group){
    var token = Sonia.History.createToken('groupPanel', group.name);
    Sonia.History.add(token);
  },

  removeGroup: function(){
    var grid = this.getGrid();
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
    this.getGrid().reload();
  }

});

// register xtype
Ext.reg('groupPanel', Sonia.group.Panel);

// register history handler
Sonia.History.register('groupPanel', {
  
  onActivate: function(panel){
    var token = null;
    var rec = panel.getGrid().getSelectionModel().getSelected();
    if (rec){
      token = Sonia.History.createToken('groupPanel', rec.get('name'));
    } else {
      token = Sonia.History.createToken('groupPanel');
    }
    return token;
  },
  
  onChange: function(repoId){
    var panel = Ext.getCmp('groups');
    if ( ! panel ){
      main.addGroupsTabPanel();
      panel = Ext.getCmp('groups');
    } else {
      main.addTab(panel);
    }
    if (repoId){
      panel.getGrid().selectById(repoId);
    }
  }
});