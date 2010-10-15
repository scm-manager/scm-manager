/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


Ext.onReady(function(){
  
  Ext.state.Manager.setProvider(new Ext.state.CookieProvider());

  var tabPanel = new Ext.TabPanel({
      region: 'center',
      deferredRender: false,
      activeTab: 0,
      items: [{
        id: 'welcome',
        xtype: 'panel',
        title: 'Welcome',
        // closable: true,
        autoScroll: true
      }]
    });

  new Ext.Viewport({
    layout: 'border',
    items: [
    new Ext.BoxComponent({
      region: 'north',
      id: 'north-panel',
      contentEl: 'north',
      height: 75
    }), {
      region: 'west',
      id: 'west',
      title: 'Navigation',
      xtype: 'navPanel',
      split: true,
      width: 200,
      minSize: 175,
      maxSize: 400,
      collapsible: true,
      margins: '0 0 0 5'
    },
    new Ext.BoxComponent({
      region: 'south',
      id: 'south-panel',
      contentEl: 'south',
      height: 16,
      margins: '2 2 2 5'
    }),
    tabPanel
  ]});

  function addTabPanel(id, xtype, title){
    tabPanel.add({
      id: id,
      xtype: xtype,
      title: title,
      closable: true,
      autoScroll: true
    });
    tabPanel.setActiveTab(id);
  }

  function addRepositoryPanel(){
    tabPanel.add({
      id: 't_repository',
      title: 'Repositories',
      xtype: 'restPanel',
      grid: {xtype: 'repositoryGrid'}
    });
    tabPanel.setActiveTab('t_repository');
  }

  function addConfigPanel(){
    addTabPanel('t_config', 'configPanel', 'Repository Config');
  }

  function logout(){
    Ext.Ajax.request({
      url: restUrl + 'authentication/logout.json',
      method: 'GET',
      success: function(response){
        tabPanel.removeAll();
        Ext.getCmp('west').removeAll();
        var loginWin = new Sonia.login.Window();
        loginWin.show();
      },
      failure: function(){
        alert("logout failed");
      }
    });
  }

  function createMainMenu(){
    var panel = Ext.getCmp('west');
    panel.addSections([{
      title: 'Main',
      items: [{
        label: 'Repositories',
        fn: addRepositoryPanel
      }]
    },{
      title: 'Config',
      items: [{
        label: 'General',
        fn: function(){ console.debug( 'General Config' ); }
      },{
        label: 'Repository Types',
        fn: addConfigPanel
      },{
        label: 'Server',
        fn: function(){ console.debug( 'Server Config' ); }
      }]
    },{
      title: 'Abmelden',
      items: [{
        label: 'Abmelden',
        fn: logout
      }]
    }]);
  }

  // create menu after login
  authCallbacks.push( createMainMenu );

  Ext.Ajax.request({
    url: restUrl + 'authentication.json',
    method: 'GET',
    success: function(response){
      var s = Ext.decode(response.responseText);
      loadState(s);
    },
    failure: function(){
      var loginWin = new Sonia.login.Window();
      loginWin.show();
    }
  });

});