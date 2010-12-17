/**
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

  var mainTabPanel = new Ext.TabPanel({
    id: 'mainTabPanel',
    region: 'center',
    deferredRender: false
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
      id: 'navigationPanel',
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
    mainTabPanel
    ]
  });

  // check login
  Ext.Ajax.request({
    url: restUrl + 'authentication.json',
    method: 'GET',
    success: function(response){
      if ( debug ){
        console.debug('login success');
      }
      var s = Ext.decode(response.responseText);
      loadState(s);
    },
    failure: function(){
      if ( debug ){
        console.debug('login failed');
      }
      var loginWin = new Sonia.login.Window();
      loginWin.show();
    }
  });

  // adds a tab to main TabPanel
  function addTabPanel(id, xtype, title){
    mainTabPanel.add({
      id: id,
      xtype: xtype,
      title: title,
      closable: true,
      autoScroll: true
    });
    mainTabPanel.setActiveTab(id);
  }

  // methods called after login

  function createMainMenu(){
    if ( debug ){
      console.debug('create main menu');
    }
    var panel = Ext.getCmp('navigationPanel');
    panel.addSection({
      id: 'navMain',
      title: 'Main',
      items: [{
        label: 'Repositories',
        fn: function(){
          addTabPanel('repositories', 'repositoryPanel', 'Repositories');
        }
      }]
    });

    if ( admin ){
      panel.addSections([{
        id: 'navConfig',
        title: 'Config',
        items: [{
          label: 'General',
          fn: function(){
            addTabPanel("scmConfig", "scmConfig", "SCM Config");
          }
        },{
          label: 'Repository Types',
          fn: function(){
            addTabPanel('repositoryConfig', 'repositoryConfig', 'Repository Config');
          }
        }]
      },{
        id: 'navPlugins',
        title: 'Plugins',
        items: [{
          label: 'Installed',
          fn: function(){
            addTabPanel('installedPlugins', 'installedPluginsGrid', 'Installed Plugins')
          }
        },{
          label: 'Available',
          fn: function(){
            addTabPanel('availablePlugins', 'availablePluginsGrid', 'Available Plugins')
          }
        }]
      },{
        title: 'Security',
        items: [{
          label: 'Users',
          fn: function(){
            addTabPanel('users', 'userPanel', 'Users');
          }
        }]
      }]);
    }
    
    panel.addSection({
      id: 'navLogout',
      title: 'Log out',
      items: [{
        label: 'Log out',
        fn: logout
      }]
    });
    //fix hidden logout button
    panel.doLayout();
  }

  function createWelcomePanel(){
    if ( debug ){
      console.debug('create welcome tab');
    }
    mainTabPanel.add({
      id: 'welcomeTab',
      xtype: 'panel',
      title: 'Welcome',
      hideMode: 'offsets',
      bodyCssClass: 'x-panel-mc',
      enableTabScroll: true,
      autoScroll: true
    });
    mainTabPanel.setActiveTab('welcomeTab');
  }

  // register login callbacks

  // create menu
  loginCallbacks.splice(0, 0, createMainMenu );
  // add welcome tab
  loginCallbacks.push( createWelcomePanel );

});