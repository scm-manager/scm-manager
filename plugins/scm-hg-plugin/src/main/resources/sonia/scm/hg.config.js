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
  }],

  onSubmit: function(values){
    Ext.Ajax.request({
      url: restUrl + 'config/repositories/hg.json',
      method: 'POST',
      jsonData: values,
      scope: this,
      success: function(response){
        alert( 'success' );
      },
      failure: function(){
        alert( 'failure' );
      }
    });
  },

  onLoad: function(){
    //this.getEl().mask();
    Ext.Ajax.request({
      url: restUrl + 'config/repositories/hg.json',
      method: 'GET',
      scope: this,
      success: function(response){
        var obj = Ext.decode(response.responseText);
        this.load(obj);
        //this.getEl().unmask();
      },
      failure: function(){
        alert( 'failure' );
        //this.getEl().unmask();
      }
    });
  }

});
