/* *
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



Sonia.config.ScmConfigPanel = Ext.extend(Sonia.config.ConfigPanel,{

  titleText: 'General Settings',
  servnameText: 'Servername',
  dateFormatText: 'Date format',
  enableForwardingText: 'Enable forwarding (mod_proxy)',
  forwardPortText: 'Forward Port',
  pluginRepositoryText: 'Plugin repository',
  allowAnonymousAccessText: 'Allow Anonymous Access',
  enableSSLText: 'Enable SSL',
  sslPortText: 'SSL Port',
  adminGroupsText: 'Admin Groups',
  adminUsersText: 'Admin Users',
  submitText: 'Submit ...',
  loadingText: 'Loading ...',
  errorTitleText: 'Error',
  errorMsgText: 'Could not load config.',
  errorSubmitMsgText: 'Could not submit config.',
  
  // TODO i18n
  enableProxyText: 'Enable Proxy',
  proxyServerText: 'Proxy Server',
  proxyPortText: 'Proxy Port',
  

  // help
  servernameHelpText: 'The name of this server. This name will be part of the repository url.',
  // TODO
  dateFormatHelpText: 'JavaScript date format.',
  pluginRepositoryHelpText: 'The url of the plugin repository.<br />Explanation of the {placeholders}:\n\
  <br /><b>version</b> = SCM-Manager Version<br /><b>os</b> = Operation System<br /><b>arch</b> = Architecture',
  enableForwardingHelpText: 'Enbale mod_proxy port forwarding.',
  forwardPortHelpText: 'The forwarding port.',
  allowAnonymousAccessHelpText: 'Anonymous users have read access on public repositories.',
  enableSSLHelpText: 'Enable secure connections via HTTPS.',
  sslPortHelpText: 'The ssl port.',
  adminGroupsHelpText: 'Comma seperated list of groups with admin permissions.',
  adminUsersHelpText: 'Comma seperated list of users with admin permissions.',
  
  // TODO i18n
  enableProxyHelpText: 'Enable Proxy',
  proxyServerHelpText: 'The proxy server',
  proxyPortHelpText: 'The proxy port',


  initComponent: function(){

    var config = {
      panels: [{
        xtype: 'configForm',
        title: this.titleText,
        items: [{
          xtype: 'textfield',
          fieldLabel: this.servnameText,
          name: 'servername',
          helpText: this.servernameHelpText,
          allowBlank: false
        },{
          xtype: 'textfield',
          fieldLabel: this.dateFormatText,
          name: 'dateFormat',
          helpText: this.dateFormatHelpText,
          allowBlank: false
        },{
          xtype: 'checkbox',
          fieldLabel: this.enableForwardingText,
          name: 'enablePortForward',
          inputValue: 'true',
          helpText: this.enableForwardingHelpText,
          listeners: {
            check: function(){
              Ext.getCmp('serverport').setDisabled( ! this.checked );
            }
          }
        },{
          id: 'serverport',
          xtype: 'numberfield',
          fieldLabel: this.forwardPortText,
          name: 'forwardPort',
          disabled: true,
          allowBlank: false,
          helpText: this.forwardPortHelpText
        },{
          xtype: 'textfield',
          fieldLabel: this.pluginRepositoryText,
          name: 'plugin-url',
          vtype: 'pluginurl',
          allowBlank: false,
          helpText: this.pluginRepositoryHelpText
        },{
          xtype: 'checkbox',
          fieldLabel: this.allowAnonymousAccessText,
          name: 'anonymousAccessEnabled',
          inputValue: 'true',
          helpText: this.allowAnonymousAccessHelpText
        },{
          xtype: 'checkbox',
          fieldLabel: this.enableSSLText,
          name: 'enableSSL',
          inputValue: 'true',
          helpText: this.enableSSLHelpText,
          listeners: {
            check: function(){
              Ext.getCmp('sslPort').setDisabled( ! this.checked );
            }
          }
        },{
          id: 'sslPort',
          xtype: 'numberfield',
          fieldLabel: this.sslPortText,
          name: 'sslPort',
          disabled: true,
          allowBlank: false,
          helpText: this.sslPortHelpText
        },{
          xtype: 'checkbox',
          fieldLabel: this.enableProxyText,
          name: 'enableProxy',
          inputValue: 'true',
          helpText: this.enableProxyHelpText,
          listeners: {
            check: function(){
              Ext.getCmp('proxyServer').setDisabled( ! this.checked );
              Ext.getCmp('proxyPort').setDisabled( ! this.checked );
            }
          }
        },{
          id: 'proxyServer',
          xtype: 'textfield',
          fieldLabel: this.proxyServerText,
          name: 'proxyServer',
          disabled: true,
          helpText: this.proxyServerHelpText,
          allowBlank: false
        },{
          id: 'proxyPort',
          xtype: 'numberfield',
          fieldLabel: this.proxyPortText,
          name: 'proxyPort',
          disabled: true,
          allowBlank: false,
          helpText: this.proxyPortHelpText
        },{
          xtype : 'textfield',
          fieldLabel : this.adminGroupsText,
          name : 'admin-groups',
          allowBlank : true,
          helpText: this.adminGroupsHelpText
        },{
          xtype : 'textfield',
          fieldLabel : this.adminUsersText,
          name : 'admin-users',
          allowBlank : true,
          helpText: this.adminUsersHelpText
        }],
      
        onSubmit: function(values){
          if ( ! values.enableSSL ){
            values.sslPort = Ext.getCmp('sslPort').getValue();
          }
          if ( ! values.enablePortForward ){
            values.forwardPort = Ext.getCmp('serverport').getValue();
          }
          this.el.mask(this.submitText);
          Ext.Ajax.request({
            url: restUrl + 'config.json',
            method: 'POST',
            jsonData: values,
            scope: this,
            disableCaching: true,
            success: function(){
              this.el.unmask();
            },
            failure: function(result){
              this.el.unmask();
              main.handleFailure(
                result.status, 
                this.errorTitleText, 
                this.errorMsgText
              );
            }
          });
        },

        onLoad: function(el){
          var tid = setTimeout( function(){ el.mask(this.loadingText); }, 100);
          Ext.Ajax.request({
            url: restUrl + 'config.json',
            method: 'GET',
            scope: this,
            disableCaching: true,
            success: function(response){
              var obj = Ext.decode(response.responseText);
              this.load(obj);
              if ( obj.enablePortForward ){
                Ext.getCmp('serverport').setDisabled(false);
              }
              if ( obj.enableSSL ){
                Ext.getCmp('sslPort').setDisabled(false);
              }
              if ( obj.enableProxy ){
                Ext.getCmp('proxyServer').setDisabled(false);
                Ext.getCmp('proxyPort').setDisabled(false);
              }
              clearTimeout(tid);
              el.unmask();
            },
            failure: function(result){
              el.unmask();
              clearTimeout(tid);
              main.handleFailure(
                result.status, 
                this.errorTitleText, 
                this.errorMsgText
              );
            }
          });
        }
      }, generalConfigPanels]
    }

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.config.ScmConfigPanel.superclass.initComponent.apply(this, arguments);
  }

});

Ext.reg("scmConfig", Sonia.config.ScmConfigPanel);
