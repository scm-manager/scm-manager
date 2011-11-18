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

Sonia.repository.DiffPanel = Ext.extend(Ext.Panel, {
  
  repository: null,
  revision: null,
  diffUrl: null,
  
  initComponent: function(){
    this.diffUrl = restUrl + 'repositories/' + this.repository.id;
    this.diffUrl += '/diff?revision=' + this.revision;
   
    var config = {
      title: 'Diff ' + this.revision,
      autoScroll: true,
      tbar: [{
        text: 'Raw',
        handler: this.downlaodFile,
        scope: this
      }],
      bbar: ['->', this.repository.name, ':', this.revision],
      items: [{
        id: 'diff-' + this.repository.id + ':' + this.revision,
        xtype: 'syntaxHighlighterPanel',
        syntax: 'diff',
        contentUrl: this.diffUrl
      }]
    }
    
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.repository.DiffPanel.superclass.initComponent.apply(this, arguments);
  },
  
  downlaodFile: function(){
    window.open(this.diffUrl);
  }
  
});

// register xtype
Ext.reg('diffPanel', Sonia.repository.DiffPanel);

// register history handler
Sonia.History.register('diffPanel', {
  
  onActivate: function(panel){
    return Sonia.History.createToken(
      'diffPanel', 
      panel.repository.id, 
      panel.revision
    );
  },
  
  onChange: function(repoId, revision){
    var id = 'diffPanel|' + repoId + '|' + revision;
    Sonia.repository.get(repoId, function(repository){
      var panel = Ext.getCmp(id);
      if (! panel){
        panel = {
          id: id,
          xtype: 'diffPanel',
          repository : repository,
          revision: revision,
          closable: true,
          autoScroll: true
        }
      }
      main.addTab(panel);
    });
  }
});