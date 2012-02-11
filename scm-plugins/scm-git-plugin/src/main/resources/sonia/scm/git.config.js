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

Ext.ns("Sonia.git");

Sonia.git.ConfigPanel = Ext.extend(Sonia.config.SimpleConfigForm, {

  // labels
  titleText: 'Git Settings',
  repositoryDirectoryText: 'Repository directory',
  disabledText: 'Disabled',

  // helpTexts
  repositoryDirectoryHelpText: 'Location of the Git repositories.',
  disabledHelpText: 'Enable or disable the Git plugin.\n\
                    Note you have to reload the page, after changing this value.',

  initComponent: function(){

    var config = {
      title : this.titleText,
      configUrl: restUrl + 'config/repositories/git.json',
      items : [{
        xtype: 'textfield',
        name: 'repositoryDirectory',
        fieldLabel: this.repositoryDirectoryText,
        helpText: this.repositoryDirectoryHelpText,
        allowBlank : false
      },{
        xtype: 'checkbox',
        name: 'disabled',
        fieldLabel: this.disabledText,
        inputValue: 'true',
        helpText: this.disabledHelpText
      }]
    }

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.git.ConfigPanel.superclass.initComponent.apply(this, arguments);
  }

});

Ext.reg("gitConfigPanel", Sonia.git.ConfigPanel);

// i18n

if ( i18n != null && i18n.country == 'de' ){

  Ext.override(Sonia.git.ConfigPanel, {

    // labels
    titleText: 'Git Einstellungen',
    repositoryDirectoryText: 'Repository-Verzeichnis',
    disabledText: 'Deaktivieren',

    // helpTexts
    repositoryDirectoryHelpText: 'Verzeichnis der Git-Repositories.',
    disabledHelpText: 'Aktivieren oder deaktivieren des Git Plugins.\n\
      Die Seite muss neu geladen werden wenn dieser Wert geändert wird.'
    
  });

}

// register information panel

initCallbacks.push(function(main){
  main.registerInfoPanel('git', {
    checkoutTemplate: 'git clone <a href="{0}" target="_blank">{0}</a>',
    xtype: 'repositoryExtendedInfoPanel'
  });
});



// register panel

registerConfigPanel({
  xtype : 'gitConfigPanel'
});
