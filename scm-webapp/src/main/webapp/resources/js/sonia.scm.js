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

Ext.ns("Sonia.scm");

Sonia.scm.Main = Ext.extend(Ext.util.Observable, {

  tabRepositoriesText: 'Repositories',
  navChangePasswordText: 'Change Password',
  sectionMainText: 'Main',
  sectionSecurityText: 'Security',
  navRepositoriesText: 'Repositories',
  sectionConfigText: 'Config',
  navGeneralConfigText: 'General',
  tabGeneralConfigText: 'SCM Config',

  navRepositoryTypesText: 'Repository Types',
  tabRepositoryTypesText: 'Repository Config',
  navPluginsText: 'Plugins',
  tabPluginsText: 'Plugins',
  navUsersText: 'Users',
  tabUsersText: 'Users',
  navGroupsText: 'Groups',
  tabGroupsText: 'Groups',

  sectionLoginText: 'Login',
  navLoginText: 'Login',

  sectionLogoutText: 'Log out',
  navLogoutText: 'Log out',


  createRepositoryPanel: function(){
    if ( debug ){
      console.debug('create repository panel');
    }
    var mainTabPanel = Ext.getCmp('mainTabPanel');
    mainTabPanel.add({
      id: 'repositories',
      xtype: 'repositoryPanel',
      title: this.tabRepositoriesText,
      closeable: false,
      autoScroll: true
    });
    mainTabPanel.setActiveTab('repositories');
  },

  createMainMenu: function(){
    if ( debug ){
      console.debug('create main menu');
    }
    var panel = Ext.getCmp('navigationPanel');
    panel.addSection({
      id: 'navMain',
      title: this.sectionMainText,
      items: [{
        label: this.navRepositoriesText,
        fn: function(){
          mainTabPanel.setActiveTab('repositories');
        }
      }]
    });

    var securitySection = null;

    if ( state.user.type == 'xml' && state.user.name != 'anonymous' ){
      securitySection = {
        title: this.sectionSecurityText,
        items: [{
          label: this.navChangePasswordText,
          fn: function(){
            new Sonia.action.ChangePasswordWindow().show();
          }
        }]
      }
    }

    if ( admin ){

      panel.addSections([{
        id: 'navConfig',
        title: this.sectionConfigText,
        items: [{
          label: this.navGeneralConfigText,
          fn: function(){
            addTabPanel("scmConfig", "scmConfig", this.navGeneralConfigText);
          }
        },{
          label: this.navRepositoryTypesText,
          fn: function(){
            addTabPanel('repositoryConfig', 'repositoryConfig', this.tabRepositoryTypesText);
          }
        },{
          label: this.navPluginsText,
          fn: function(){
            addTabPanel('plugins', 'pluginGrid', this.navPluginsText);
          }
        }]
      }]);

      if ( securitySection == null ){
        securitySection = {
          title: this.sectionSecurityText,
          items: []
        }
      }

      securitySection.items.push({
        label: this.navUsersText,
        fn: function(){
          addTabPanel('users', 'userPanel', this.navUsersText);
        }
      });
      securitySection.items.push({
        label: this.navGroupsText,
        fn: function(){
          addTabPanel('groups', 'groupPanel', this.tabGroupsText);
        }
      });
    }

    if ( securitySection != null ){
      panel.addSection( securitySection );
    }

    if ( state.user.name == 'anonymous' ){
      panel.addSection({
        id: 'navLogin',
        title: this.sectionLoginText,
        items: [{
          label: this.sectionLoginText,
          fn: login
        }]
      });
    } else {
      panel.addSection({
        id: 'navLogout',
        title: this.sectionLogoutText,
        items: [{
          label: this.navLogoutText,
          fn: logout
        }]
      });
    }

    //fix hidden logout button
    panel.doLayout();
  }

});

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

  checkLogin();

  // adds a tab to main TabPanel
  function addTabPanel(id, xtype, title){
    var tab = mainTabPanel.findById( id );
    if ( tab == null ){
      mainTabPanel.add({
        id: id,
        xtype: xtype,
        title: title,
        closable: true,
        autoScroll: true
      });
    }
    mainTabPanel.setActiveTab(id);
  }

  // register login callbacks

  var main = new Sonia.scm.Main();

  // create menu
  loginCallbacks.splice(0, 0, {fn: main.createMainMenu, scope: main});
  // add repository tab
  loginCallbacks.push({fn: main.createRepositoryPanel, scope: main});

});