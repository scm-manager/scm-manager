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

// config form panels
var repositoryConfigPanels = [];
var generalConfigPanels =[];

function registerConfigPanel(panel){
  repositoryConfigPanels.push( panel );
}

function registerGeneralConfigPanel(panel){
  generalConfigPanels.push(panel);
}

Ext.ns("Sonia.config");

Sonia.config.ConfigPanel = Ext.extend(Ext.Panel, {

  panels: null,

  initComponent: function(){

    var config = {
      region: 'center',
      bodyCssClass: 'x-panel-mc',
      trackResetOnLoad: true,
      autoScroll: true,
      border: false,
      frame: false,
      collapsible: false,
      collapsed: false,
      items: this.panels
    }

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.config.ConfigPanel.superclass.initComponent.apply(this, arguments);
  }

});

Ext.reg("configPanel", Sonia.config.ConfigPanel);

Sonia.config.RepositoryConfig = Ext.extend(Sonia.config.ConfigPanel,{

   initComponent: function(){

    var config = {
      panels: repositoryConfigPanels
    }

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.config.RepositoryConfig.superclass.initComponent.apply(this, arguments);
  }

});

Ext.reg("repositoryConfig", Sonia.config.RepositoryConfig);

Sonia.config.ScmConfigPanel = Ext.extend(Sonia.config.ConfigPanel,{

  initComponent: function(){

    var config = {
      panels: [{
        xtype: 'configForm',
        title: 'General Settings',
        items: [{
          xtype: 'textfield',
          fieldLabel: 'Servername',
          name: 'servername',
          allowBlank: false
        },{
          xtype: 'textfield',
          fieldLabel: 'Plugin repository',
          name: 'plugin-url',
          vtype: 'url',
          allowBlank: false
        },{
          xtype: 'checkbox',
          fieldLabel: 'Enable SSL',
          name: 'enableSSL',
          inputValue: 'true'
        },{
          xtype: 'textfield',
          fieldLabel: 'SSL Port',
          name: 'sslPort',
          vtype: 'alphanum'
        }],
      
        onSubmit: function(values){
          this.el.mask('Submit ...');
          Ext.Ajax.request({
            url: restUrl + 'config.json',
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
            url: restUrl + 'config.json',
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
              Ext.MessageBox.show({
                title: 'Error',
                msg: 'Could not load config.',
                buttons: Ext.MessageBox.OK,
                icon:Ext.MessageBox.ERROR
              });
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

Sonia.config.ConfigForm = Ext.extend(Ext.form.FormPanel, {

  title: 'Config Form',
  items: null,
  onSubmit: null,
  getValues: null,

  initComponent: function(){

    var config = {
      title: null,
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
        title : this.title,
        collapsible : true,
        autoHeight : true,
        labelWidth : 140,
        buttonAlign: 'left',
        layoutConfig : {
          labelSeparator : ''
        },
        defaults: {
          width: 250
        },
        listeners: {
          render: function(){
            if ( this.onLoad != null && Ext.isFunction( this.onLoad ) ){
              this.onLoad(this.el);
            }
          },
          scope: this
        },
        items: this.items,
        buttons: [{
          text: 'Save',
          scope: this,
          formBind: true,
          handler: this.submitForm
        },{
          text: 'Reset',
          scope: this,
          handler: function(){
            this.getForm().reset();
          }
        }]
      }]
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.config.ConfigForm.superclass.initComponent.apply(this, arguments);
  },

  load: function(values){
    this.getForm().loadRecord({
      success: true,
      data: values
    });
  },

  submitForm: function(){
    var form = this.getForm();
    if ( this.onSubmit != null && Ext.isFunction( this.onSubmit ) ){
      this.onSubmit( form.getValues() );
    }
  }

});

Ext.reg("configForm", Sonia.config.ConfigForm);
