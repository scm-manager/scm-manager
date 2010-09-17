/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
registerConfigPanel({
  xtype : 'configForm',
  title : 'Mercurial Settings',
  items : [{
    xtype : 'textfield',
    fieldLabel : 'HG Binary',
    name : 'hgBinary',
    allowBlank : false
  },{
    xtype: 'textfield',
    name: 'repositoryDirectory',
    fieldLabel: 'Repository directory',
    allowBlank : false
  },{
    xtype: 'textfield',
    name: 'baseUrl',
    fieldLabel: 'Base URL',
    allowBlank : false
  }]
});
