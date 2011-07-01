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

Ext.ns("Sonia.ldap");

Sonia.ldap.ConfigPanel = Ext.extend(Sonia.config.ConfigForm, {
  
  titleText: 'LDAP Authentication',
  idAttributeText: 'ID Attribute Name',
  fullnameAttributeText: 'Fullname Attribute Name',
  mailAttributeText: 'Mail Attribute Name',
  groupAttributeText: 'Group Attribute Name',
  baseDNText: 'Base DN',
  connectionDNText: 'Connection DN',
  connectionPasswordText: 'Connection Password',
  hostURLText: 'Host URL',
  searchFilterText: 'Search Filter',
  searchScopeText: 'Search Scope',
  groupsUnitText: 'Groups Unit',
  peopleUnitText: 'People Unit',
  enabledText: 'Enabled',
  
  // help texts
  idAttributeHelpText: 'The name of the ldap attribute which contains the username',
  fullnameAttributeHelpText: 'The name of the ldap attribute which contains the displayname of the user',
  mailAttributeHelpText: 'The name of the ldap attribute which contains the e-mail address of the user',
  // TODO improve
  groupAttributeHelpText: 'The name of the ldap attribute which contains the group names of the user',
  baseDNHelpText: 'The basedn for example: dc=example,dc=com',
  connectionDNHelpText: 'The complete dn of the connection user. <strong>Note:<strong> \n\
                         This user needs read an search privileges for the id, mail and fullname attributes.',
  connectionPasswordHelpText: 'The password for connection user.',
  hostURLHelpText: 'The url for the ldap server. For example: ldap://localhost:389/',
  searchFilterHelpText: 'The search filter to find the users. <strong>Note:</strong>\n\
                        {0} will be replaced by the username.',
  searchScopeHelpText: 'The scope for the user search.',
  peopleUnitHelpText: 'The relative location of the users. For example: ou=People',
  groupsUnitHelpText: 'The relative location of the users. For example: ou=Groups',
  enabledHelpText: 'Enables or disables the ldap authentication',
  
  initComponent: function(){
    
    var config = {
      title : this.titleText,
      items : [{
        xtype : 'textfield',
        fieldLabel : this.idAttributeText,
        name : 'attribute-name-id',
        allowBlank : true,
        helpText: this.idAttributeHelpText
      },{
        xtype : 'textfield',
        fieldLabel : this.fullnameAttributeText,
        name : 'attribute-name-fullname',
        allowBlank : true,
        helpText: this.fullnameAttributeHelpText
      },{
        xtype : 'textfield',
        fieldLabel : this.mailAttributeText,
        name : 'attribute-name-mail',
        allowBlank : true,
        helpText: this.mailAttributeHelpText
      },{
        xtype : 'textfield',
        fieldLabel : this.groupAttributeText,
        name : 'attribute-name-group',
        allowBlank : true,
        helpText: this.groupAttributeHelpText
      },{
        xtype : 'textfield',
        fieldLabel : this.baseDNText,
        name : 'base-dn',
        allowBlank : true,
        helpText: this.baseDNHelpText
      },{
        xtype : 'textfield',
        fieldLabel : this.connectionDNText,
        name : 'connection-dn',
        allowBlank : true,
        helpText: this.connectionDNHelpText
      },{
        xtype : 'textfield',
        inputType: 'password',
        fieldLabel : this.connectionPasswordText,
        name : 'connection-password',
        allowBlank : true,
        helpText: this.connectionPasswordHelpText
      },{
        xtype : 'textfield',
        fieldLabel : this.hostURLText,
        name : 'host-url',
        allowBlank : true,
        helpText: this.hostURLHelpText
      },{
        xtype : 'textfield',
        fieldLabel : this.searchFilterText,
        name : 'search-filter',
        allowBlank : true,
        helpText: this.searchFilterHelpText
      },{
        xtype : 'combo',
        fieldLabel : this.searchScopeText,
        name : 'search-scope',
        allowBlank : true,
        helpText: this.searchScopeHelpText,
        valueField: 'scope',
        displayField: 'scope',
        typeAhead: false,
        editable: false,
        triggerAction: 'all',
        mode: 'local',
        store: new Ext.data.SimpleStore({
          fields: ['scope'],
          data: [
            ['object'],
            ['one'],
            ['sub']
          ]
        })
      },{
        xtype : 'textfield',
        fieldLabel : this.peopleUnitText,
        name : 'unit-people',
        allowBlank : true,
        helpText: this.peopleUnitHelpText
      },{
        xtype : 'textfield',
        fieldLabel : this.groupsUnitText,
        name : 'unit-groups',
        allowBlank : true,
        helpText: this.groupsUnitHelpText
      },{
        xtpye: 'checkbox',
        fieldLabel : this.enabledText,
        name: 'enabled',
        helpText: this.enabledHelpText
      }]
    }
    
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.ldap.ConfigPanel.superclass.initComponent.apply(this, arguments);
  },
  
  onSubmit: function(values){
    this.el.mask(this.submitText);
    Ext.Ajax.request({
      url: restUrl + 'config/auth/ldap.json',
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
    var tid = setTimeout( function(){el.mask(this.loadingText);}, 100);
    Ext.Ajax.request({
      url: restUrl + 'config/auth/ldap.json',
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

// register xtype
Ext.reg("ldapConfigPanel", Sonia.ldap.ConfigPanel);


// i18n

if ( i18n != null && i18n.country == 'de' ){

  Ext.override(Sonia.ldap.ConfigPanel, {

    // TODO

  });

}

// regist config panel
registerGeneralConfigPanel({
  id: 'ldapConfigPanel',
  xtype: 'ldapConfigPanel'
});
