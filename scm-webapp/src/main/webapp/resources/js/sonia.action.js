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

Ext.ns('Sonia.action');

Sonia.action.ChangePasswordWindow = Ext.extend(Ext.Window,{

  initComponent: function(){

    var config = {
      layout:'fit',
      width:300,
      height:170,
      closable: false,
      resizable: false,
      plain: true,
      border: false,
      modal: true,
      items: [{
        id: 'changePasswordForm',
        url: restUrl + 'action/change-password.json',
        title: 'Change Password',
        frame: true,
        xtype: 'form',
        monitorValid: true,
        defaultType: 'textfield',
        items: [{
          name: 'old-password',
          fieldLabel: 'Old Password',
          inputType: 'password',
          allowBlank: false,
          minLength: 6,
          maxLength: 32
        },{
          id: 'new-password',
          name: 'new-password',
          fieldLabel: 'New Password',
          inputType: 'password',
          allowBlank: false,
          minLength: 6,
          maxLength: 32
        },{
          name: 'confirm-password',
          fieldLabel: 'Confirm Password',
          inputType: 'password',
          allowBlank: false,
          minLength: 6,
          maxLength: 32,
          vtype: 'password',
          initialPassField: 'new-password'
        }],
        buttons: [{
          text: 'Ok',
          formBind: true,
          scope: this,
          handler: this.changePassword
        },{
          text: 'Cancel',
          scope: this,
          handler: this.cancel
        }]
      }]
    }
    
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.action.ChangePasswordWindow.superclass.initComponent.apply(this, arguments);
  },

  changePassword: function(){
    var win = this;
    var form = Ext.getCmp('changePasswordForm').getForm();
    form.submit({
      method:'POST',
      waitTitle:'Connecting',
      waitMsg:'Sending data...',

      success: function(form, action){
        if ( debug ){
          console.debug( 'change password success' );
        }
        win.close();
      },

      failure: function(form, action){
        if ( debug ){
          console.debug( 'change password failed' );
        }
        Ext.Msg.alert('change password failed!');
      }
    });
  },

  cancel: function(){
    this.close();
  }

});