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
Sonia.repository.ContentPanel = Ext.extend(Ext.Panel, {
  
  repository: null,
  revision: null,
  path: null,
  contentUrl: null,
  
  initComponent: function(){
    var name = Sonia.util.getName(this.path);
    this.contentUrl = Sonia.repository.createContentUrl(
      this.repository, this.path, this.revision
    );
    
    var bottomBar = [this.path];
    this.appendRepositoryProperties(bottomBar);
    
    var config = {
      title: name,
      tbar: [{
        text: 'Default',
        handler: this.openSyntaxPanel,
        scope: this
      },{
        text: 'Raw',
        handler: this.downlaodFile,
        scope: this
      },{
        text: 'Blame',
        handler: this.openBlamePanel,
        scope: this
      },{
        text: 'History',
        handler: this.openHistoryPanel,
        scope: this
      }],
      bbar: bottomBar,
      items: [{
        xtype: 'syntaxHighlighterPanel',
        syntax: Sonia.util.getExtension(this.path),
        contentUrl: this.contentUrl
      }]
    }
    
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.repository.ContentPanel.superclass.initComponent.apply(this, arguments);
  },
  
  openHistoryPanel: function(){
    this.openPanel({
      xtype: 'repositoryChangesetViewerPanel',
      repository: this.repository,
      revision: this.revision,
      path: this.path,
      inline: true,
      // TODO find a better way
      pageSize: 9999
    });
  },
  
  openSyntaxPanel: function(){
    this.openPanel({
      xtype: 'syntaxHighlighterPanel',
      syntax: Sonia.util.getExtension(this.path),
      contentUrl: this.contentUrl
    });
  },
  
  openBlamePanel: function(){
    this.openPanel({
      xtype: 'blamePanel',
      repository: this.repository,
      revision: this.revision,
      path: this.path
    });
  },
  
  downlaodFile: function(){
    window.open(this.contentUrl);
  },
  
  openPanel: function(panel){
    this.removeAll();
    this.add(panel);
    this.doLayout();
  },
  
  appendRepositoryProperties: function(bar){
    bar.push('->',this.repository.name);
    if ( this.revision != null ){
      bar.push(': ', this.revision);
    }
  },
  
  getExtension: function(path){
    var ext = null;
    var index = path.lastIndexOf('.');
    if ( index > 0 ){
      ext = path.substr(index + 1, path.length);
    }
    return ext;
  }
  
});


Ext.reg('contentPanel', Sonia.repository.ContentPanel);