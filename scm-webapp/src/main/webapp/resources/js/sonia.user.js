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

// UserGrid
Sonia.user.Grid = Ext.extend(Sonia.rest.Grid, {

  initComponent: function(){

    var userStore = new Sonia.rest.JsonStore({
      url: restUrl + 'users.json',
      root: 'users',
      fields: [ 'name', 'displayName', 'mail', 'type'],
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
        {id: 'name', header: 'Name', dataIndex: 'name'},
        {id: 'displayName', header: 'Display Name', dataIndex: 'displayName', width: 250},
        {id: 'mail', header: 'Mail', dataIndex: 'mail', renderer: this.renderMailto, width: 200},
        {id: 'type', header: 'Type', dataIndex: 'type', width: 80}
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
    console.debug( item );
  }

});

// register xtype
Ext.reg('userGrid', Sonia.user.Grid);


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

  initComponent: function(){

    var config = {
      items: [{
        fieldLabel: 'Name',
        name: 'name',
        allowBlank: false
      },{
        fieldLabel: 'DisplayName',
        name: 'displayName',
        allowBlank: false
      },{
        fieldLabel: 'Mail',
        name: 'mail',
        allowBlank: false,
        vtype: 'email'
      },{
        fieldLabel: 'Password',
        id: 'pwd',
        name: 'password',
        inputType: 'password',
        minLength: 6,
        maxLength: 32,
        minLengthText: 'Password must be at least 6 characters long.'
      },{
        name: 'password',
        inputType: 'password',
        minLength: 6,
        maxLength: 32,
        minLengthText: 'Password must be at least 6 characters long.',
        vtype: 'password',
        initialPassField: 'pwd'
      }]
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.user.FormPanel.superclass.initComponent.apply(this, arguments);
  },

  update: function(item){
    console.debug('update user');
    console.debug(item);
  },

  create: function(user){
    if ( debug ){
      console.debug( 'create user: ' + user.name );
    }
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
        alert( 'failure' );
      }
    });
  }

});

// register xtype
Ext.reg('userForm', Sonia.user.FormPanel);

// UserPanel
Sonia.user.Panel = Ext.extend(Ext.Panel, {

  initComponent: function(){

    var config = {
      layout: 'border',
      hideMode: 'offsets',
      bodyCssClass: 'x-panel-mc',
      enableTabScroll: true,
      region:'center',
      autoScroll: true,
      tbar: [
        {xtype: 'tbbutton', text: 'Add', scope: this, handler: this.showAddPanel},
        {xtype: 'tbbutton', text: 'Remove', scope: this, handler: this.remove},
        '-',
        {xtype: 'tbbutton', text: 'Reload', scope: this, handler: this.reload},
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
          html: 'Add or select an User'
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
      title: 'Repository Form',
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

  remove: function(){
    console.debug( 'remove' );
  },

  reload: function(){
    console.debug( 'reload' );
  }

});

// register xtype
Ext.reg('userPanel', Sonia.user.Panel);
