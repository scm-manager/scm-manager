/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

Ext.ns('Sonia.login');

Sonia.login.Form = Ext.extend(Ext.FormPanel,{

  initComponent: function(){

    var config = {
      labelWidth: 80,
      url: restUrl + "authentication/login.json",
      frame: true,
      title: 'Please Login',
      defaultType: 'textfield',
      monitorValid: true,
      listeners: {
        afterrender: function(){
          Ext.getCmp('username').focus(true, 500);
        }
      },
      items:[{
        id: 'username',
        fieldLabel:'Username',
        name:'username',
        allowBlank:false,
        listeners: {
          specialkey: {
            fn: this.specialKeyPressed,
            scope: this
          }
        }
      },{
        fieldLabel:'Password',
        name:'password',
        inputType:'password',
        allowBlank:false,
        listeners: {
          specialkey: {
            fn: this.specialKeyPressed,
            scope: this
          }
        }
      }],
      buttons:[{
        text:'Login',
        formBind: true,
        scope: this,
        handler: this.authenticate
      }]
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.login.Form.superclass.initComponent.apply(this, arguments);
  },

  authenticate: function(){
    var form = this.getForm();
    form.submit({
      method:'POST',
      waitTitle:'Connecting',
      waitMsg:'Sending data...',

      success: function(form, action){
        loadState( action.result );
      },

      failure: function(form, action){
        Ext.Msg.alert('Login Failure!');
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

Sonia.login.Window = Ext.extend(Ext.Window,{

  initComponent: function(){

    var form = new Sonia.login.Form();
    form.on('actioncomplete', function(){
      this.fireEvent('success');
      this.close();
    }, this);

    var config = {
      layout:'fit',
      width:300,
      height:150,
      closable: false,
      resizable: false,
      plain: true,
      border: false,
      modal: true,
      items: [form]
    };

    this.addEvents('success');

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.login.Window.superclass.initComponent.apply(this, arguments);

  }

});