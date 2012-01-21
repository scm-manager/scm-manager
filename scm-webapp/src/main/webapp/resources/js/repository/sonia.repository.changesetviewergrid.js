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

// changeset viewer

Sonia.repository.ChangesetViewerGrid = Ext.extend(Ext.grid.GridPanel, {

  repository: null,
  
  mailTemplate: '&lt;<a href="mailto: {0}">{0}</a>&gt;',
  
  changesetMetadataTemplate: '<div class="cs-desc">{0}</div>\
                              <div class="cs-author">{1}</div>\
                              <div class="cs-date">{2}</div>',
  
  modificationsTemplate: '<div class="cs-mod">\
                            <img src="resources/images/add.png" alt="Added"><span class="cs-mod-txt">{0}</span>\
                            <img src="resources/images/modify.gif" alt="Modified"><span class="cs-mod-txt">{1}</span>\
                            <img src="resources/images/delete.png" alt="Deleted"><span class="cs-mod-txt">{2}</span>\
                          </div>',
  
  idsTemplate: new Ext.XTemplate('<div class="cs-commit">Commit: <a class="scm-link cs-diff-link" rel="{id}">{id}</a></div>\
                <div class="cs-tree">Source: <a class="scm-link cs-tree-link" rel="{id}">{id}</a></div>\
                <tpl if="parent"><div class="cs-parent">Parent: <a class="scm-link cs-diff-link" rel="{parent}">{parent}</a></div></tpl>\
                <tpl if="parent2"><div class="cs-parent">Parent: <a class="scm-link cs-diff-link" rel="{parent2}">{parent2}</a></div></tpl>'),
  
  tagsAndBranchesTemplate: new Ext.XTemplate('<div class="changeset-tags"><tpl if="tags"><tpl for="tags"><span class="cs-tag"><a title="Tag {.}">{.}</a></span></tpl></tpl></div>\
                            <div class="changeset-branches"><tpl if="branches"><tpl for="branches"><span class="cs-branch"><a title="Branch {.}">{.}</a></span></tpl></tpl></div>'),
  
  emptyText: 'No commits available',

  initComponent: function(){

    var changesetColModel = new Ext.grid.ColumnModel({
      defaults: {
        sortable: false
      },
      columns: [{
        id: 'metadata',
        dataIndex: 'author',
        renderer: this.renderChangesetMetadata,
        scope: this
      },{
        id: 'tagsAndBranches',
        renderer: this.renderTagsAndBranches,
        scope: this
      },{
        id: 'modifications',
        dataIndex: 'modifications',
        renderer: this.renderModifications,
        scope: this,
        width: 120
      },{
        id: 'ids',
        dataIndex: 'id',
        renderer: this.renderIds,
        scope: this,
        width: 180
      }]
    });

    var config = {
      header: false,
      autoScroll: true,
      autoExpandColumn: 'metadata',
      autoHeight: true,
      hideHeaders: true,
      colModel: changesetColModel,
      loadMask: true,
      viewConfig: {
        deferEmptyText: false,
        emptyText: this.emptyText
      },
      listeners: {
        click: {
          fn: this.onClick,
          scope: this
        }
      }
    }
    
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.repository.ChangesetViewerGrid.superclass.initComponent.apply(this, arguments);
  },
  
  getRevision: function(el){
    var revision = el.rel;
    var index = revision.indexOf(':');
    if ( index >= 0 ){
      revision = revision.substr(index+1);
    }
    return revision;
  },

  onClick: function(e){
    var el = e.getTarget('.cs-tree-link');
    var revision = null;
    
    if (el){
      revision = this.getRevision(el);
      if (debug){
        console.debug('load repositorybrowser for ' + revision);
      }
      
      this.openRepositoryBrowser(revision);
    } else {      
      el = e.getTarget('.cs-diff-link');

      if ( el ){
        revision = this.getRevision(el);
        if (debug){
          console.debug('load diff for ' + revision);
        }

       this.openChangeset(revision);
      }
    }
  },
  
  openChangeset: function(revision){
    main.addTab({
      id: 'changesetPanel;' + this.repository.id + ';' + revision,
      xtype: 'changesetPanel',
      repository: this.repository,
      revision: revision,
      closable: true
    });    
  },
  
  openRepositoryBrowser: function(revision){
    main.addTab({
      id: 'repositoryBrowser;' + this.repository.id + ';' + revision,
      xtype: 'repositoryBrowser',
      repository: this.repository,
      revision: revision,
      closable: true
    });
  },

  renderChangesetMetadata: function(author, p, record){
    var authorValue = '';
    if ( author != null ){
      authorValue = Ext.util.Format.htmlEncode(author.name);
      if ( author.mail != null ){
        authorValue += ' ' + String.format(this.mailTemplate, author.mail);
      }
    }
    var description = record.data.description;
    // if ( description != null ){
    //  description = Ext.util.Format.htmlEncode(description);
    // }
    var date = record.data.date;
    if ( date != null ){
      date = Ext.util.Format.formatTimestamp(date);
    }
    return String.format(
      this.changesetMetadataTemplate,
      description,
      authorValue,
      date
    );
  },

  renderTagsAndBranches: function(value, p, record){
    return this.tagsAndBranchesTemplate.apply({
      tags: record.get('tags'),
      branches: record.get('branches')
    });
  },

  getLabeledValue: function(label, array){
    var result = '';
    if ( array != null && array.length > 0 ){
      result = label + ': ' + Sonia.util.getStringFromArray(array);
    }
    return result;
  },

  renderIds: function(value, p, record){
    var parent = null;
    var parent2 = null;
    var parents = record.get('parents');
    if ( parents ){
      parent = parents[0];
      if ( parents.length >= 1 ){
        parent2 = parents[1];
      }
    }
    return this.idsTemplate.apply({
      id: value,
      parent: parent,
      parent2: parent2
    });
  },

  renderModifications: function(value){
    var added = 0;
    var modified = 0;
    var removed = 0;
    if ( Ext.isDefined(value) ){
      if ( Ext.isDefined(value.added) ){
        added = value.added.length;
      }
      if ( Ext.isDefined(value.modified) ){
        modified = value.modified.length;
      }
      if ( Ext.isDefined(value.removed) ){
        removed = value.removed.length;
      }
    }
    return String.format(this.modificationsTemplate, added, modified, removed);
  }

});

// register xtype
Ext.reg('repositoryChangesetViewerGrid', Sonia.repository.ChangesetViewerGrid);

