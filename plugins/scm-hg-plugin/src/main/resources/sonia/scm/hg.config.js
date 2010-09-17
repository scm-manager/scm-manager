/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
repositoryConfigPanels.push({
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
    title : 'Mercurial Settings',
    collapsible : true,
    autoHeight : true,
    labelWidth : 140,
    buttonAlign: 'left',
    layoutConfig : {
      labelSeparator : ''
    },
    items : [{
      xtype : 'textfield',
      fieldLabel : 'HG Binary',
      name : 'hgBinary',
      allowBlank : false
    },{
      xtype: 'textfield',
      name: 'hgRepoDirectroy',
      fieldLabel: 'Repository directory',
      allowBlank : false
    },{
      xtype: 'textfield',
      name: 'hgBaseUrl',
      fieldLabel: 'Base URL',
      allowBlank : false
    }],
    buttons: [{
      text: 'Save'
    },{
      text: 'Cancel'
    }]
  }]

});
