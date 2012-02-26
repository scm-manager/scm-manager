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

Sonia.repository.ChangesetPanel = Ext.extend(Ext.Panel, {

  repository: null,
  revision: null,
  view: 'commit',
  
  // labels
  title: 'Commit {0}',
  commitLabel: 'Commmit',
  diffLabel: 'Diff',
  rawDiffLabel: 'Raw Diff',

  initComponent: function(){
    var panel = null;
    
    switch (this.view){
      case 'diff':
        panel = this.createDiffPanel();
        break;
      default:
        panel = this.createCommitPanel();
    }
    
    var config = {
      title: String.format(this.title, this.revision),
      autoScroll: true,
      tbar: [{
        text: this.commitLabel,
        handler: this.showCommit,
        scope: this
      },{
        text: this.diffLabel,
        handler: this.showDiff,
        scope: this          
      },{
        text: this.rawDiffLabel,
        handler: this.downloadRawDiff,
        scope: this
      }],
      layout: 'fit',
      bbar: ['->', this.repository.name, ':', this.revision],
      items: [panel]
    }

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.repository.ChangesetPanel.superclass.initComponent.apply(this, arguments);
  },
  
  openPanel: function(panel){
    this.removeAll();
    this.add(panel);
    this.doLayout();
  },
  
  updateHistory: function(){
    var token = Sonia.History.createToken(
      'changesetPanel', 
      this.repository.id, 
      this.revision,
      this.view
    );
    Sonia.History.add(token);
  },
  
  createCommitPanel: function(){
    return {
      id: 'commit-' + this.repository.id + ':' + this.revision,
      xtype: 'commitPanel',
      repository: this.repository,
      revision: this.revision
    }
  },
  
  createDiffPanel: function(){
    return {
      id: 'diff-' + this.repository.id + ':' + this.revision,
      xtype: 'syntaxHighlighterPanel',
      syntax: 'diff',
      contentUrl: this.createDiffUrl()
    }
  },
  
  createDiffUrl: function(){
    var diffUrl = restUrl + 'repositories/' + this.repository.id;
    return diffUrl + '/diff?revision=' + this.revision;
  },
  
  showCommit: function(){
    if ( console ){
      console.debug('open commit for ' + this.revision);
    }
    this.openPanel(this.createCommitPanel());
    this.view = 'commit';
    this.updateHistory();
  },
  
  showDiff: function(){
    if ( console ){
      console.debug('open diff for ' + this.revision);
    }
    this.openPanel(this.createDiffPanel());
    this.view = 'diff';
    this.updateHistory();
  },
  
  downloadRawDiff: function(){
    if ( console ){
      console.debug('open raw diff for ' + this.revision);
    }
    window.open(this.createDiffUrl());
  }
  
});

// register xtype
Ext.reg('changesetPanel', Sonia.repository.ChangesetPanel);

// register history handler
Sonia.History.register('changesetPanel', {
  
  onActivate: function(panel){
    return Sonia.History.createToken(
      'changesetPanel', 
      panel.repository.id, 
      panel.revision, 
      panel.view
    );
  },
  
  onChange: function(repoId, revision, view){
    if (revision == 'null'){
      revision = null;
    }
    if (!view || view == 'null'){
      view = 'commit';
    }
    var id = 'changesetPanel;' + repoId + ';' + revision;
    Sonia.repository.get(repoId, function(repository){
      var panel = Ext.getCmp(id);
      if (! panel){
        panel = {
          id: id,
          xtype: 'changesetPanel',
          repository : repository,
          revision: revision,
          view: view,
          closable: true,
          autoScroll: true
        }
      } else {
        panel.loadPanel(view);
      }
      main.addTab(panel);
    });
  }
});