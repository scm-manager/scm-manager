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

Sonia.login.Form = Ext.extend(Ext.FormPanel,{

  usernameText: 'Username',
  passwordText: 'Password',
  loginText: 'Login',
  cancelText: 'Cancel',
  waitTitleText: 'Connecting',
  WaitMsgText: 'Sending data...',
  failedMsgText: 'Login failed!',
  failedDescriptionText: 'Incorrect username, password or not enough permission. Please Try again.',

  initComponent: function(){

    var config = {
      labelWidth: 80,
      url: restUrl + "authentication/login.json",
      frame: true,
      title: this.titleText,
      defaultType: 'textfield',
      monitorValid: true,
      listeners: {
        afterrender: function(){
          Ext.getCmp('username').focus(true, 500);
        }
      },
      items:[{
        id: 'username',
        fieldLabel: this.usernameText,
        name: 'username',
        allowBlank:false,
        listeners: {
          specialkey: {
            fn: this.specialKeyPressed,
            scope: this
          }
        }
      },{
        fieldLabel: this.passwordText,
        name: 'password',
        inputType: 'password',
        allowBlank: false,
        listeners: {
          specialkey: {
            fn: this.specialKeyPressed,
            scope: this
          }
        }
      }],
      buttons:[{
          text: this.cancelText,
          scope: this,
          handler: this.cancel
        },{
          id: 'loginButton',
          text: this.loginText,
          formBind: true,
          scope: this,
          handler: this.authenticate
      }]
    };

    this.addEvents('cancel', 'failure');

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.login.Form.superclass.initComponent.apply(this, arguments);
  },

  cancel: function(){
    this.fireEvent('cancel');
    main.checkLogin();
  },

  authenticate: function(){
    var form = this.getForm();
    form.submit({
      scope: this,
      method: 'POST',
      waitTitle: this.waitTitleText,
      waitMsg: this.WaitMsgText,

      success: function(form, action){
        if ( debug ){
          console.debug( 'login success' );
        }
        main.loadState( action.result );
      },

      failure: function(form){
        if ( debug ){
          console.debug( 'login failed' );
        }
        this.fireEvent('failure');
        Ext.Msg.show({
          title: this.failedMsgText,
          msg: this.failedDescriptionText,
          buttons: Ext.Msg.OK,
          icon: Ext.MessageBox.WARNING
        });
        form.reset();
      }
    });
  },

  specialKeyPressed: function(field, e){
    if (e.getKey() == e.ENTER) {
      var form = this.getForm();
      if ( form.isValid() ){
        this.authenticate();
      }
    }
  }

});

Ext.reg('soniaLoginForm', Sonia.login.Form);
