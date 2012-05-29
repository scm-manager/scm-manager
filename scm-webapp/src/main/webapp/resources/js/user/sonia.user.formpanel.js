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

Sonia.user.FormPanel = Ext.extend(Sonia.rest.FormPanel,{

  nameText: 'Name',
  displayNameText: 'Display name',
  mailText: 'Mail',
  passwordText: 'Password',
  adminText: 'Administrator',
  activeText: 'Active',
  errorTitleText: 'Error',
  updateErrorMsgText: 'User update failed',
  createErrorMsgText: 'User creation failed',
  passwordMinLengthText: 'Password must be at least 6 characters long',

  // help
  usernameHelpText: 'Unique name of the user.',
  displayNameHelpText: 'Display name of the user.',
  mailHelpText: 'Email address of the user.',
  passwordHelpText: 'Plain text password of the user.',
  passwordConfirmHelpText: 'Repeat the password for validation.',
  adminHelpText: 'An administrator is able to create, modify and delete repositories, groups and users.',
  activeHelpText: 'Activate or deactive the user.',

  initComponent: function(){

    var items = [{
      fieldLabel: this.nameText,
      name: 'name',
      allowBlank: false,
      readOnly: this.item != null,
      helpText: this.usernameHelpText,
      vtype: 'username'
    },{
      fieldLabel: this.displayNameText,
      name: 'displayName',
      allowBlank: false,
      readOnly: this.isReadOnly(),
      helpText: this.displayNameHelpText
    },{
      fieldLabel: this.mailText,
      name: 'mail',
      allowBlank: true,
      vtype: 'email',
      readOnly: this.isReadOnly(),
      helpText: this.mailHelpText
    }];

    if ( this.item == null || this.item.type == state.defaultUserType ){
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
    },{
      fieldLabel: this.activeText,
      name: 'active',
      xtype: 'checkbox',
      helpText: this.activeHelpText,
      checked: true
    });

    Ext.apply(this, Ext.apply(this.initialConfig, {items: items}));
    Sonia.user.FormPanel.superclass.initComponent.apply(this, arguments);
  },
  
  isReadOnly: function(){
    return this.item != null && this.item.type != state.defaultUserType;
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
      failure: function(result){
        main.handleRestFailure(
          result, 
          this.errorTitleText, 
          this.updateErrorMsgText
        );
      }
    });
    
  },

  create: function(user){
    if ( debug ){
      console.debug( 'create user: ' + user.name );
    }
    this.fixRequest(user);
    // set user type
    user.type = state.defaultUserType;
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
      failure: function(result){
        main.handleRestFailure(
          result,
          this.errorTitleText, 
          this.createErrorMsgText
        );
      }
    });
  },

  cancel: function(){
    Sonia.user.setEditPanel( Sonia.user.DefaultPanel );
  }

});

// register xtype
Ext.reg('userForm', Sonia.user.FormPanel);
