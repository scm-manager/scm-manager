/*
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
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
      closable: true,
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