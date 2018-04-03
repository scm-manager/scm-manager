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
  pythonPathText: 'Python Module Search Path',
  repositoryDirectoryText: 'Repository directory',
  useOptimizedBytecodeText: 'Optimized Bytecode (.pyo)',
  configWizardText: 'Start Configuration Wizard',
  configWizardLabelText: 'Start Configuration Wizard',
  encodingText: 'Encoding',
  disabledText: 'Disabled',
  showRevisionInIdText: 'Show Revision',
  // TODO: i18n
  disableHookSSLValidationText: 'Disable SSL Validation on Hooks',
  enableHttpPostArgsText: 'Enable HttpPostArgs Protocol',

  // helpText
  hgBinaryHelpText: 'Location of Mercurial binary.',
  pythonBinaryHelpText: 'Location of Python binary.',
  pythonPathHelpText: 'Python Module Search Path (PYTHONPATH).',
  repositoryDirectoryHelpText: 'Location of the Mercurial repositories.',
  useOptimizedBytecodeHelpText: 'Use the Python "-O" switch.',
  encodingHelpText: 'Repository Encoding.',
  disabledHelpText: 'Enable or disable the Mercurial plugin. \n\
                    Note you have to reload the page, after changing this value.',
  showRevisionInIdHelpText: 'Show revision as part of the node id. Note: \n\
          You have to restart the ApplicationServer to affect cached changesets.',
  // TODO: i18n
  disableHookSSLValidationHelpText: 'Disables the validation of ssl certificates for the mercurial hook, which forwards the repository changes back to scm-manager. \n\
                                     This option should only be used, if SCM-Manager uses a self signed certificate.',
  // TODO explain it
  enableHttpPostArgsHelpText: 'Enables the experimental HttpPostArgs Protocol of mercurial.\n\
    The HttpPostArgs Protocol uses the body of post requests to send the meta information instead of http headers.\
    This helps to reduce the header size of mercurial requests. HttpPostArgs is supported since mercurial 3.8.',

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
        xtype: 'textfield',
        name: 'encoding',
        fieldLabel: this.encodingText,
        helpText: this.encodingHelpText,
        allowBlank : false
      },{
        xtype: 'checkbox',
        name: 'useOptimizedBytecode',
        fieldLabel: this.useOptimizedBytecodeText,
        inputValue: 'true',
        helpText: this.useOptimizedBytecodeHelpText
      },{
        xtype: 'checkbox',
        name: 'showRevisionInId',
        fieldLabel: this.showRevisionInIdText,
        inputValue: 'true',
        helpText: this.showRevisionInIdHelpText
      },{
        xtype: 'checkbox',
        name: 'disableHookSSLValidation',
        fieldLabel: this.disableHookSSLValidationText,
        inputValue: 'true',
        helpText: this.disableHookSSLValidationHelpText
      },{
        xtype: 'checkbox',
        name: 'enableHttpPostArgs',
        fieldLabel: this.enableHttpPostArgsText,
        inputValue: 'true',
        helpText: this.enableHttpPostArgsHelpText
      },{
        xtype: 'checkbox',
        name: 'disabled',
        fieldLabel: this.disabledText,
        inputValue: 'true',
        helpText: this.disabledHelpText
      },{
        xtype: 'button',
        text: this.configWizardText,
        fieldLabel: this.configWizardLabelText,
        handler: function(){
          var config = this.getForm().getValues();
          var wizard = new Sonia.hg.ConfigWizard({
            hgConfig: config
          });
          wizard.on('finish', function(config){
            var self = Ext.getCmp('hgConfigForm');
            if ( config ){
              if (debug){
                console.debug( 'load config from wizard and submit to server' );
              }
              self.loadConfig( self.el, 'config/repositories/hg/auto-configuration.json', 'POST', config );
            } else {
              if (debug){
                console.debug( 'reload config' );
              }
              self.onLoad(self.el);
            }
          }, this);
          wizard.show();
        },
        scope: this
      }]
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.hg.ConfigPanel.superclass.initComponent.apply(this, arguments);
  },

  onSubmit: function(values){
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

  loadConfig: function(el, url, method, config){
    var tid = setTimeout( function(){ el.mask(this.loadingText); }, 100);
    Ext.Ajax.request({
      url: restUrl + url,
      method: method,
      jsonData: config,
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

// i18n

if ( i18n && i18n.country === 'de' ){

  Ext.override(Sonia.hg.ConfigPanel, {

    // labels
    titleText: 'Mercurial Einstellungen',
    hgBinaryText: 'HG Pfad',
    pythonBinaryText: 'Python Pfad',
    pythonPathText: 'Python Modul Suchpfad',
    repositoryDirectoryText: 'Repository-Verzeichnis',
    useOptimizedBytecodeText: 'Optimierter Bytecode (.pyo)',
    autoConfigText: 'Einstellungen automatisch laden',
    autoConfigLabelText: 'Automatische Einstellung',
    configWizardText: 'Konfigurations-Assistenten starten',
    configWizardLabelText: 'Konfigurations-Assistent',
    disabledText: 'Deaktivieren',
    showRevisionInIdText: 'Zeige Revision an',

    // helpText
    hgBinaryHelpText: 'Pfad zum "hg" Befehl.',
    pythonBinaryHelpText: 'Pfad zum "python" Befehl.',
    pythonPathHelpText: 'Python Modul Suchpfad (PYTHONPATH).',
    repositoryDirectoryHelpText: 'Verzeichnis der Mercurial-Repositories.',
    useOptimizedBytecodeHelpText: 'Optimierten Bytecode verwenden (python -O).',
    disabledHelpText: 'Aktivieren oder deaktivieren des Mercurial Plugins.\n\
      Die Seite muss neu geladen werden wenn dieser Wert geändert wird.',
    showRevisionInIdHelpText: 'Zeige die Revision als teil der NodeId an. \n\
          Der ApplicationServer muss neugestartet werden um zwischengespeicherte\n\
           Changesets zuändern.'
  });

}

// register information panel

initCallbacks.push(function(main){
  main.registerInfoPanel('hg', {
    checkoutTemplate: 'hg clone <a href="{0}" target="_blank">{0}</a>',
    xtype: 'repositoryExtendedInfoPanel'
  });
});

// register config panel

registerConfigPanel({
  id: 'hgConfigForm',
  xtype : 'hgConfigPanel'
});

// register type icon

Sonia.repository.typeIcons['hg'] = 'resources/images/icons/16x16/mercurial.png';

// override ChangesetViewerGrid to render changeset id's with revisions

Ext.override(Sonia.repository.ChangesetViewerGrid, {

  isMercurialRepository: function(){
    return this.repository.type === 'hg';
  },

  getChangesetId: function(id, record){
    if ( this.isMercurialRepository() ){
      var rev = Sonia.util.getProperty(record.get('properties'), 'hg.rev');
      if ( rev ){
        id = rev + ':' + id;
      }
    }
    return id;
  },

  getParentIds: function(id, record){
    var parents = record.get('parents');
    if ( this.isMercurialRepository() ){
      if ( parents && parents.length > 0 ){
        var properties = record.get('properties');
        var rev = Sonia.util.getProperty(properties, 'hg.p1.rev');
        if (rev){
          parents[0] = rev + ':' + parents[0];
        }
        if ( parents.length > 1 ){
          rev = Sonia.util.getProperty(properties, 'hg.p2.rev');
          if (rev){
            parents[1] = rev + ':' + parents[1];
          }
        }
      }
    }
    return parents;
  }

});
