/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
registerConfigPanel({
  xtype : 'configForm',
  title : 'Subversion Settings',
  items : [{
    xtype : 'textfield',
    fieldLabel : 'Svnadmin Binary',
    name : 'svnAdminBinary',
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
  },{
    xtype: 'textfield',
    name: 'svnAccessFile',
    fieldLabel: 'Svn Accessfile',
    allowBlank : true
  }],

  onSubmit: function(values){
    this.el.mask('Submit ...');
    Ext.Ajax.request({
      url: restUrl + 'config/repositories/svn.json',
      method: 'POST',
      jsonData: values,
      scope: this,
      disableCaching: true,
      success: function(response){
        this.el.unmask();
      },
      failure: function(){
        this.el.unmask();
      }
    });
  },

  onLoad: function(el){
    var tid = setTimeout( function(){ el.mask('Loading ...'); }, 100);
    Ext.Ajax.request({
      url: restUrl + 'config/repositories/svn.json',
      method: 'GET',
      scope: this,
      disableCaching: true,
      success: function(response){
        var obj = Ext.decode(response.responseText);
        this.load(obj);
        clearTimeout(tid);
        el.unmask();
      },
      failure: function(){
        el.unmask();
        clearTimeout(tid);
        alert('failure');
      }
    });
  }

});
