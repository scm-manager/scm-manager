/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

Ext.ns("Sonia.config");

Sonia.config.ConfigPanel = Ext.extend(Ext.Panel, {

  initComponent: function(){

    var config = {
      region: 'center',
      bodyCssClass: 'x-panel-mc',
      trackResetOnLoad: true,
      autoScroll: true,
      border: false,
      frame: false,
      collapsible: false,
      collapsed: false,
      items: repositoryConfigPanels
    }

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.config.ConfigPanel.superclass.initComponent.apply(this, arguments);
  }

});

Ext.reg("configPanel", Sonia.config.ConfigPanel);

Sonia.config.ConfigForm = Ext.extend(Ext.form.FormPanel, {

  title: 'Config Form',
  items: null,
  onSubmit: null,
  getValues: null,
  onCancel: null,

  initComponent: function(){

    var config = {
      title: null,
      style: 'margin: 10px',
      trackResetOnLoad : true,
      autoScroll : true,
      border : false,
      frame : false,
      collapsible : false,
      collapsed : false,
      layoutConfig : {
        labelSeparator : ''
      },
      items : [{
        xtype : 'fieldset',
        checkboxToggle : false,
        title : this.title,
        collapsible : true,
        autoHeight : true,
        labelWidth : 140,
        buttonAlign: 'left',
        layoutConfig : {
          labelSeparator : ''
        },
        items: this.items,
        buttons: [{
          text: 'Save',
          scope: this,
          handler: this.submitForm
        },{
          text: 'Cancel',
          scope: this,
          handler: this.cancel
        }]
      }]
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.config.ConfigForm.superclass.initComponent.apply(this, arguments);
    
    if ( this.onLoad != null && Ext.isFunction( this.onLoad ) ){
      this.onLoad();
    }
  },

  load: function(values){
    this.getForm().loadRecord({success: true, data: values});
  },

  submitForm: function(){
    var form = this.getForm();
    if ( this.onSubmit != null && Ext.isFunction( this.onSubmit ) ){
      this.onSubmit( form.getValues() );
    }
  },

  cancel: function(){
    var form = this.getForm();
    if ( this.onCancel != null && Ext.isFunction( this.onCancel ) ){
      this.onCancel(form);
    }
  }

});

Ext.reg("configForm", Sonia.config.ConfigForm);
