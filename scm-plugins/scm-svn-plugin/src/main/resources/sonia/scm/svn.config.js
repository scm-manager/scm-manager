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

Ext.ns("Sonia.svn");

Sonia.svn.ConfigPanel = Ext.extend(Sonia.config.SimpleConfigForm, {

  // labels
  titleText: 'Subversion Settings',
  repositoryDirectoryText: 'Repository directory',
  noneCompatibility: 'No compatibility modus',
  pre14CompatibleText: 'Pre 1.4 Compatible',
  pre15CompatibleText: 'Pre 1.5 Compatible',
  pre16CompatibleText: 'Pre 1.6 Compatible',
  pre17CompatibleText: 'Pre 1.7 Compatible',
  with17CompatibleText: 'With 1.7 Compatible',
  enableGZipText: 'Enable GZip Encoding',
  disabledText: 'Disabled',

  // helpTexts
  repositoryDirectoryHelpText: 'Location of the Suberversion repositories.',
  disabledHelpText: 'Enable or disable the Subversion plugin.\n\
                    Note you have to reload the page, after changing this value.',
  enableGZipHelpText: 'Enable GZip encoding for svn responses.',

  initComponent: function(){

    var config = {
      title : this.titleText,
      configUrl: restUrl + 'config/repositories/svn.json',
      items : [{
        xtype: 'textfield',
        name: 'repositoryDirectory',
        fieldLabel: this.repositoryDirectoryText,
        helpText: this.repositoryDirectoryHelpText,
        allowBlank : false
      },{
        xtype: 'radiogroup',
        name: 'compatibility',
        columns: 1,
        items: [{
          boxLabel: this.noneCompatibility, 
          inputValue: 'NONE',
          name: 'compatibility'
        },{
          boxLabel: this.pre14CompatibleText, 
          inputValue: 'PRE14',
          name: 'compatibility'
        },{
          boxLabel: this.pre15CompatibleText, 
          inputValue: 'PRE15',
          name: 'compatibility'
        },{
          boxLabel: this.pre16CompatibleText, 
          inputValue: 'PRE16',
          name: 'compatibility'
        },{
          boxLabel: this.pre17CompatibleText, 
          inputValue: 'PRE17',
          name: 'compatibility'
        },{
          boxLabel: this.with17CompatibleText, 
          inputValue: 'WITH17',
          name: 'compatibility'
        }]
      },{
        xtype: 'checkbox',
        name: 'enable-gzip',
        fieldLabel: this.enableGZipText,
        inputValue: 'true',
        helpText: this.enableGZipHelpText        
      },{
        xtype: 'checkbox',
        name: 'disabled',
        fieldLabel: this.disabledText,
        inputValue: 'true',
        helpText: this.disabledHelpText
      }]
    }

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.svn.ConfigPanel.superclass.initComponent.apply(this, arguments);
  }

});

Ext.reg("svnConfigPanel", Sonia.svn.ConfigPanel);

// i18n

if ( i18n != null && i18n.country == 'de' ){

  Ext.override(Sonia.svn.ConfigPanel, {

    // labels
    titleText: 'Subversion Einstellungen',
    repositoryDirectoryText: 'Repository-Verzeichnis',
    noneCompatibility: 'Kein Kompatiblitätsmodus',
    pre14CompatibleText: 'Mit Versionen vor 1.4 kompatibel',
    pre15CompatibleText: 'Mit Versionen vor 1.5 kompatibel',
    pre16CompatibleText: 'Mit Versionen vor 1.6 kompatibel',
    pre17CompatibleText: 'Mit Versionen vor 1.7 kompatibel',
    with17CompatibleText: 'Mit Version 1.7 kompatibel',
    disabledText: 'Deaktivieren',

    // helpTexts
    repositoryDirectoryHelpText: 'Verzeichnis der Subversion-Repositories.',
    disabledHelpText: 'Aktivieren oder deaktivieren des Subversion Plugins.\n\
      Die Seite muss neu geladen werden wenn dieser Wert geändert wird.'
  });

}

// register information panel

initCallbacks.push(function(main){
  main.registerInfoPanel('svn', {
    checkoutTemplate: 'svn checkout <a href="{0}" target="_blank">{0}</a>',
    xtype: 'repositoryExtendedInfoPanel'
  });
});

// register panel

registerConfigPanel({
  xtype : 'svnConfigPanel'
});
