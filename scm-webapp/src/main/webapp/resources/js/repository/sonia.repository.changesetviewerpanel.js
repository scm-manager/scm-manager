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


Sonia.repository.ChangesetViewerPanel = Ext.extend(Ext.Panel, {

  repository: null,
  start: 0,
  limit: -1,
  pageSize: 20,
  historyId: null,
  
  changesetViewerTitleText: 'Commits {0}',
  
  initComponent: function(){
    this.historyId = 'changesetviewer|' + this.repository.id;

    var changesetStore = new Sonia.rest.JsonStore({
      id: 'changesetStore',
      proxy: new Ext.data.HttpProxy({
        url: restUrl + 'repositories/' + this.repository.id  + '/changesets.json',
        method: 'GET'
      }),
      fields: ['id', 'date', 'author', 'description', 'modifications', 'tags', 'branches', 'properties'],
      root: 'changesets',
      idProperty: 'id',
      totalProperty: 'total',
      autoLoad: true,
      autoDestroy: true,
      baseParams: {
        start: this.start,
        limit: this.limit > 0 ? this.limit : this.pageSize
      },
      listeners: {
        load: {
          fn: this.updateHistory,
          scope: this
        }
      }
    });

    var config = {
      title: String.format(this.changesetViewerTitleText, this.repository.name),
      items: [{
        xtype: 'repositoryChangesetViewerGrid',
        repository: this.repository,
        store: changesetStore
      }],
      bbar: {
        xtype: 'paging',
        store: changesetStore,
        displayInfo: true,
        pageSize: this.pageSize,
        prependButtons: true
      }
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.repository.ChangesetViewerPanel.superclass.initComponent.apply(this, arguments);
  },
  
  updateHistory: function(store, records, options){
    var id = Sonia.History.appendWithDepth([options.params.start, options.params.limit], 2);
    if (id){
      this.historyId = id;
    }
  }

});

// register xtype
Ext.reg('repositoryChangesetViewerPanel', Sonia.repository.ChangesetViewerPanel);

// register history handler
Sonia.History.register('changesetviewer', function(params){
  
  if (params){
  
    Ext.Ajax.request({
      url: restUrl + 'repositories/' + params[0] + '.json',
      method: 'GET',
      scope: this,
      success: function(response){
        var item = Ext.decode(response.responseText);
        main.addTab({
          id: item.id + '-changesetViewer',
          xtype: 'repositoryChangesetViewerPanel',
          repository: item,
          start: parseInt(params[1]),
          limit: parseInt(params[2]),
          closable: true
        })
      },
      failure: function(result){
        main.handleFailure(
          result.status
        );
      }
    });
  
  }
  
});
