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

Sonia.user.Panel = Ext.extend(Sonia.rest.Panel, {

  titleText: 'User Form',
  emptyText: 'Add or select an User',
  removeTitleText: 'Remove User',
  removeMsgText: 'Remove User "{0}"?',
  errorTitleText: 'Error',
  errorMsgText: 'User deletion failed',
  
  // userGrid for history
  userGrid: null,

  initComponent: function(){

    var config = {
      bodyCssClass: 'x-panel-mc',
      tbar: [
        {xtype: 'tbbutton', text: this.addText, icon: this.addIcon, scope: this, handler: this.showAddPanel},
        {xtype: 'tbbutton', text: this.removeText, icon: this.removeIcon, scope: this, handler: this.removeUser},
        '-',
        {xtype: 'tbbutton', text: this.reloadText, icon: this.reloadIcon, scope: this, handler: this.reload}
      ],
      items: [{
        id: 'userGrid',
        xtype: 'userGrid',
        region: 'center',
        parentPanel: this
      },{
        id: 'userEditPanel',
        layout: 'fit',
        items: [{
          region: 'south',
          title: 'User Form',
          xtype: 'panel',
          padding: 5,
          html: this.emptyText
        }],
        height: 250,
        split: true,
        border: false,
        region: 'south'
      }]
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.user.Panel.superclass.initComponent.apply(this, arguments);
  },
  
  getGrid: function(){
    if (!this.userGrid){
      if (debug){
        console.debug('userGrid not found retrvie by id');
      }
      this.userGrid = Ext.getCmp('userGrid');
    }
    return this.userGrid;
  },

  showAddPanel: function(){
    var editPanel = Ext.getCmp('userEditPanel');
    editPanel.removeAll();
    var panel = new Sonia.user.FormPanel({
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
    editPanel.add(panel);
    editPanel.doLayout();
  },
  
  resetPanel: function(){
   Sonia.user.setEditPanel( Sonia.user.DefaultPanel );
  },

  removeUser: function(){

    var grid = this.getGrid();
    var selected = grid.getSelectionModel().getSelected();
    if ( selected ){
      var item = selected.data;
      var url = restUrl + 'users/' + item.name + '.json';

      Ext.MessageBox.show({
        title: this.removeTitleText,
        msg: String.format(this.removeMsgText, item.name),
        buttons: Ext.MessageBox.OKCANCEL,
        icon: Ext.MessageBox.QUESTION,
        fn: function(result){
          if ( result == 'ok' ){

            if ( debug ){
              console.debug( 'remove user ' + item.name );
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
      console.debug( 'no user selected' );
    }

  },

  reload: function(){
    this.getGrid().reload();
  }

});

// register xtype
Ext.reg('userPanel', Sonia.user.Panel);


// register history handler
Sonia.History.register('userPanel', {
  
  onActivate: function(panel){
    var token = null;
    var rec = panel.getGrid().getSelectionModel().getSelected();
    if (rec){
      token = Sonia.History.createToken('userPanel', rec.get('name'));
    } else {
      token = Sonia.History.createToken('userPanel');
    }
    return token;
  },
  
  onChange: function(userId){
    var panel = Ext.getCmp('users');
    if ( ! panel ){
      main.addUsersTabPanel();
      panel = Ext.getCmp('users');
      if ( userId ){
        var selected = false;
        panel.getGrid().getStore().addListener('load', function(){
          if (!selected){
            panel.getGrid().selectById(userId);
            selected = true;
          }
        });
      }
    } else {
      main.addTab(panel);
      if (userId){
        panel.getGrid().selectById(userId);
      }
    }
  }
});