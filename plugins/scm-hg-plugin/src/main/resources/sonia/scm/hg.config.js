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


Ext.ns("Sonia.hg");

Sonia.hg.ConfigPanel = Ext.extend(Sonia.config.ConfigForm, {

  // labels
  titleText: 'Mercurial Settings',
  hgBinaryText: 'HG Binary',
  pythonBinaryText: 'Python Binary',
  pythonPathText: 'Python Path',
  repositoryDirectoryText: 'Repository directory',
  useOptimizedBytecodeText: 'Optimized Bytecode (.pyo)',
  autoConfigText: 'Load Auto-Configuration',

  // helpText
  hgBinaryHelpText: 'The location of the Mercurial binary.',
  pythonBinaryHelpText: 'The location of the Python binary.',
  pythonPathHelpText: 'The Python path.',
  repositoryDirectoryHelpText: 'The location of the Mercurial repositories.',
  useOptimizedBytecodeHelpText: 'Use the Python "-O" switch.',

  initComponent: function(){

    var config = {
      title : this.titleText,
      items : [{
        xtype : 'textfield',
        fieldLabel : this.hgBinaryText,
        name : 'hgBinary',
        allowBlank : false,
        helpText: this.hgBinaryHelpText
      },{
        xtype : 'textfield',
        fieldLabel : this.pythonBinaryText,
        name : 'pythonBinary',
        allowBlank : false,
        helpText: this.pythonBinaryHelpText
      },{
        xtype : 'textfield',
        fieldLabel : this.pythonPathText,
        name : 'pythonPath',
        helpText: this.pythonPathHelpText
      },{
        xtype: 'textfield',
        name: 'repositoryDirectory',
        fieldLabel: this.repositoryDirectoryText,
        helpText: this.repositoryDirectoryHelpText,
        allowBlank : false
      },{
        xtype: 'checkbox',
        name: 'useOptimizedBytecode',
        fieldLabel: this.useOptimizedBytecodeText,
        inputValue: 'true',
        helpText: this.useOptimizedBytecodeHelpText
      },{
        xtype: 'button',
        text: this.autoConfigText,
        fieldLabel: 'Auto-Configuration',
        handler: function(){
          var self = Ext.getCmp('hgConfigForm');
          self.loadConfig( self.el, 'config/repositories/hg/auto-configuration.json', 'POST' );
        }
      }]
    }

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.hg.ConfigPanel.superclass.initComponent.apply(this, arguments);
  },

  onSubmit: function(values){

    console.debug( this.submitText );

    console.debug( this );

    this.el.mask(this.submitText);
    Ext.Ajax.request({
      url: restUrl + 'config/repositories/hg.json',
      method: 'POST',
      jsonData: values,
      scope: this,
      disableCaching: true,
      success: function(){
        this.el.unmask();
      },
      failure: function(){
        this.el.unmask();
        alert('failure');
      }
    });
  },

  onLoad: function(el){
    this.loadConfig(el, 'config/repositories/hg.json', 'GET');
  },

  loadConfig: function(el, url, method){
    var tid = setTimeout( function(){ el.mask(this.loadingText); }, 100);
    Ext.Ajax.request({
      url: restUrl + url,
      method: method,
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

Ext.reg("hgConfigPanel", Sonia.hg.ConfigPanel);

registerConfigPanel({
  id: 'hgConfigForm',
  xtype : 'hgConfigPanel'
});
