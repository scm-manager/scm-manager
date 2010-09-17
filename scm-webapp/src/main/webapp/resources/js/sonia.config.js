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