/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

Ext.ns('Sonia.login');

Sonia.login.Form = Ext.extend(Ext.FormPanel,{

  initComponent: function(){

    var config = {
      labelWidth:80,
      url: restUrl + "authentication.json",
      frame:true,
      title:'Please Login',
      defaultType:'textfield',
      monitorValid:true,
      items:[{
        fieldLabel:'Username',
        name:'username',
        allowBlank:false
      },{
        fieldLabel:'Password',
        name:'password',
        inputType:'password',
        allowBlank:false
      }],
      buttons:[{
        text:'Login',
        formBind: true,
        scope: this,
        handler: function(){
          var form = this.getForm();
          form.submit({
            method:'POST',
            waitTitle:'Connecting',
            waitMsg:'Sending data...',
            
            success: function(){
              Ext.Msg.alert('Login Success!');
            },

            failure: function(form, action){
              Ext.Msg.alert('Login Failure!');
              form.reset();
            }
          });
        }
      }]
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.login.Form.superclass.initComponent.apply(this, arguments);

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