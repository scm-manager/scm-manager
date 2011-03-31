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


Ext.ns("Sonia.bzr");

Sonia.bzr.ConfigPanel = Ext.extend(Sonia.config.SimpleConfigForm, {

  // labels
  titleText: 'Bazaar Settings',
  bzrBinaryText: 'Bzr Binary',
  pythonBinary: 'Python Binary',
  pythonPath: 'Python Path',
  repositoryDirectoryText: 'Repository directory',

  // helpTexts
  bzrBinaryHelpText: 'The location of the Bzr binary.',
  pythonBinaryHelpText: 'The location of the Python binary.',
  pythonPathHelpText: 'The Python path.',
  repositoryDirectoryHelpText: 'The location of the Bazaar repositories.',

  initComponent: function(){

    var config = {
      title : this.titleText,
      configUrl: restUrl + 'config/repositories/bzr.json',
      items : [{
        xtype : 'textfield',
        fieldLabel : this.bzrBinaryText,
        name : 'bzrBinary',
        allowBlank : false,
        helpText: this.bzrBinaryHelpText
      },{
        xtype : 'textfield',
        fieldLabel : this.pythonBinary,
        name : 'pythonBinary',
        allowBlank : false,
        helpText: this.pythonBinaryHelpText
      },{
        xtype : 'textfield',
        fieldLabel : this.pythonPath,
        name : 'pythonPath',
        helpText: this.pythonPathHelpText
      },{
        xtype: 'textfield',
        name: 'repositoryDirectory',
        fieldLabel: this.repositoryDirectoryText,
        allowBlank : false,
        helpText: this.repositoryDirectoryHelpText
      }]
    }

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.bzr.ConfigPanel.superclass.initComponent.apply(this, arguments);
  }

});

Ext.reg("bzrConfigPanel", Sonia.bzr.ConfigPanel);

// i18n

if ( i18n != null && i18n.country == 'de' ){

  Ext.override(Sonia.hg.ConfigPanel, {

    // labels
    titleText: 'Bazaar Settings',
    bzrBinaryText: 'Bzr Pfad',
    pythonBinary: 'Python Pfad',
    pythonPath: 'Python Modul Suchpfad',
    repositoryDirectoryText: 'Repository-Verzeichnis',

    // helpTexts
    bzrBinaryHelpText: 'Pfad zum "bzr" Befehl.',
    pythonBinaryHelpText: 'Pfad zum "python" Befehl.',
    pythonPathHelpText: 'Der Python Modul Suchpfad (PYTHONPATH).',
    repositoryDirectoryHelpText: 'The location of the Bazaar repositories.'

  });

}

// register panel

registerConfigPanel({
  xtype : 'bzrConfigPanel'
});
