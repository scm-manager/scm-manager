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
Ext.ns('Sonia.user');


// functions

Sonia.user.setEditPanel = function(panel){
  var editPanel = Ext.getCmp('userEditPanel');
  editPanel.removeAll();
  editPanel.add(panel);
  editPanel.doLayout();
}

// Panels

Sonia.user.DefaultPanel = {
  region: 'south',
  title: 'User Form',
  padding: 5,
  xtype: 'panel',
  html: 'Add or select an User'
};

// UserGrid
Sonia.user.Grid = Ext.extend(Sonia.rest.Grid, {

  titleText: 'User Form',
  colNameText: 'Name',
  colDisplayNameText: 'Display Name',
  colMailText: 'Mail',
  colAdminText: 'Admin',
  colCreationDateText: 'Creation Date',
  colLastModifiedText: 'Last modified',
  colTypeText: 'Type',

  initComponent: function(){

    var userStore = new Sonia.rest.JsonStore({
      proxy: new Ext.data.HttpProxy({
        url: restUrl + 'users.json',
        disableCaching: false
      }),
      fields: [ 'name', 'displayName', 'mail', 'admin', 'creationDate', 'lastModified', 'type'],
      sortInfo: {
        field: 'name'
      }
    });

    var userColModel = new Ext.grid.ColumnModel({
      defaults: {
        sortable: true,
        scope: this,
        width: 125
      },
      columns: [
        {id: 'name', header: this.colNameText, dataIndex: 'name'},
        {id: 'displayName', header: this.colDisplayNameText, dataIndex: 'displayName', width: 250},
        {id: 'mail', header: this.colMailText, dataIndex: 'mail', renderer: this.renderMailto, width: 200},
        {id: 'admin', header: this.colAdminText, dataIndex: 'admin', renderer: this.renderCheckbox, width: 50},
        {id: 'creationDate', header: this.colCreationDateText, dataIndex: 'creationDate'},
        {id: 'lastModified', header: this.colLastModifiedText, dataIndex: 'lastModified'},
        {id: 'type', header: this.colTypeText, dataIndex: 'type', width: 80}
      ]
    });

    var config = {
      store: userStore,
      colModel: userColModel
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.user.Grid.superclass.initComponent.apply(this, arguments);
  },

  selectItem: function(item){
    if ( debug ){
      console.debug( item.name + ' selected' );
    }
    var panel = new Sonia.user.FormPanel({
      item: item,
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
    if ( item.type == 'xml' ){
      panel.getForm().setValues([
        {id: 'password', value: dummyPassword},
        {id: 'password-confirm', value: dummyPassword}
      ]);
    }
    Sonia.user.setEditPanel(panel);
  }

});

// register xtype
Ext.reg('userGrid', Sonia.user.Grid);

// passord validator
Ext.apply(Ext.form.VTypes, {
  password: function(val, field) {
    if (field.initialPassField) {
      var pwd = Ext.getCmp(field.initialPassField);
      return (val == pwd.getValue());
    }
    return true;
  },
  passwordText: 'The passwords entered do not match!'
});


// UserFormPanel
Sonia.user.FormPanel = Ext.extend(Sonia.rest.FormPanel,{

  nameText: 'Name',
  displayNameText: 'Display name',
  mailText: 'Mail',
  passwordText: 'Password',
  adminText: 'Administrator',
  errorTitleText: 'Error',
  updateErrorMsgText: 'User update failed',
  createErrorMsgText: 'User creation failed',
  passwordMinLengthText: 'Password must be at least 6 characters long',

  // help
  usernameHelpText: 'The unique name for the user.',
  displayNameHelpText: 'The display name for the user.',
  mailHelpText: 'The email address for the user.',
  passwordHelpText: 'The plain text password for the user.',
  passwordConfirmHelpText: 'Repeat the password for validation.',
  adminHelpText: 'An administrator is able to create, modify and delete repositories, groups and users.',

  initComponent: function(){

    var items = [{
      fieldLabel: this.nameText,
      name: 'name',
      allowBlank: false,
      readOnly: this.item != null,
      helpText: this.usernameHelpText
    },{
      fieldLabel: this.displayNameText,
      name: 'displayName',
      allowBlank: false,
      helpText: this.displayNameHelpText
    },{
      fieldLabel: this.mailText,
      name: 'mail',
      allowBlank: true,
      vtype: 'email',
      helpText: this.mailHelpText
    }];

    if ( this.item == null || this.item.type == 'xml' ){
      items.push({
        fieldLabel: this.passwordText,
        id: 'pwd',
        name: 'password',
        inputType: 'password',
        minLength: 6,
        maxLength: 32,
        minLengthText: this.passwordMinLengthText,
        helpText: this.passwordHelpText
      },{
        name: 'password-confirm',
        inputType: 'password',
        minLength: 6,
        maxLength: 32,
        minLengthText: this.passwordMinLengthText,
        vtype: 'password',
        initialPassField: 'pwd',
        helpText: this.passwordConfirmHelpText
      });
    }

    items.push({
      fieldLabel: this.adminText,
      name: 'admin',
      xtype: 'checkbox',
      helpText: this.adminHelpText
    });

    Ext.apply(this, Ext.apply(this.initialConfig, {items: items}));
    Sonia.user.FormPanel.superclass.initComponent.apply(this, arguments);
  },

  fixRequest: function(user){
    delete user['password-confirm'];
  },

  update: function(item){

    item = Ext.apply( this.item, item );
    if ( debug ){
      console.debug( 'update user: ' + item.name );
    }
    this.fixRequest(item);
    var url = restUrl + 'users/' + item.name + '.json';
    Ext.Ajax.request({
      url: url,
      jsonData: item,
      method: 'PUT',
      scope: this,
      success: function(){
        if ( debug ){
          console.debug('update success');
        }
        this.execCallback(this.onUpdate, item);
      },
      failure: function(){
        Ext.MessageBox.show({
          title: this.errorTitleText,
          msg: this.updateErrorMsgText,
          buttons: Ext.MessageBox.OK,
          icon:Ext.MessageBox.ERROR
        });
      }
    });
    
  },

  create: function(user){
    if ( debug ){
      console.debug( 'create user: ' + user.name );
    }
    this.fixRequest(user);
    // set user type
    user.type = 'xml';
    var url = restUrl + 'users.json';
    Ext.Ajax.request({
      url: url,
      jsonData: user,
      method: 'POST',
      scope: this,
      success: function(){
        if ( debug ){
          console.debug('create success');
        }
        this.getForm().reset();
        this.execCallback(this.onCreate, user);
      },
      failure: function(){
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
    Sonia.user.setEditPanel( Sonia.user.DefaultPanel );
  }

});

// register xtype
Ext.reg('userForm', Sonia.user.FormPanel);

// UserPanel
Sonia.user.Panel = Ext.extend(Ext.Panel, {

  addText: 'Add',
  removeText: 'Remove',
  reloadText: 'Reload',
  titleText: 'User Form',
  emptyText: 'Add or select an User',
  removeTitleText: 'Remove User',
  removeMsgText: 'Remove User "{0}"?',
  errorTitleText: 'Error',
  errorMsgText: 'User deletion failed',

  initComponent: function(){

    var config = {
      layout: 'border',
      hideMode: 'offsets',
      bodyCssClass: 'x-panel-mc',
      enableTabScroll: true,
      region:'center',
      autoScroll: true,
      tbar: [
        {xtype: 'tbbutton', text: this.addText, scope: this, handler: this.showAddPanel},
        {xtype: 'tbbutton', text: this.removeText, scope: this, handler: this.removeUser},
        '-',
        {xtype: 'tbbutton', text: this.reloadText, scope: this, handler: this.reload}
      ],
      items: [{
        id: 'userGrid',
        xtype: 'userGrid',
        region: 'center'
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

    var grid = Ext.getCmp('userGrid');
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
      console.debug( 'no user selected' );
    }

  },

  reload: function(){
    Ext.getCmp('userGrid').reload();
  }

});

// register xtype
Ext.reg('userPanel', Sonia.user.Panel);
