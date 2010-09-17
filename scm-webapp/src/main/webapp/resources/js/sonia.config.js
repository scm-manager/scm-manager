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
          text: 'Save'
        },{
          text: 'Cancel'
        }]
      }]
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.config.ConfigForm.superclass.initComponent.apply(this, arguments);
  }

});

Ext.reg("configForm", Sonia.config.ConfigForm);
