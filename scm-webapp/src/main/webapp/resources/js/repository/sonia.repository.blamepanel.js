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


Sonia.repository.BlamePanel = Ext.extend(Ext.grid.GridPanel, {
  
  blameUrl: null,
  repository: null,
  path: null,
  revision: null,
  linkTemplate: '<a class="scm-link blame-link" rel="{1}">{0}</a>',
  
  initComponent: function(){
    var blameUrl = restUrl + 'repositories/' + this.repository.id  + '/';
    blameUrl += 'blame.json?path=' + this.path;
    if ( this.revision ){
      blameUrl += "&revision=" + this.revision;
    }
    
    var blameStore = new Sonia.rest.JsonStore({
      proxy: new Ext.data.HttpProxy({
        url: blameUrl,
        disableCaching: false
      }),
      root: 'blamelines',
      idProperty: 'lineNumber',
      fields: [ 'lineNumber', 'author', 'revision', 'when', 'description', 'code'],
      sortInfo: {
        field: 'lineNumber'
      }
    });

    var blameColModel = new Ext.grid.ColumnModel({
      columns: [{
        id: 'revision', 
        dataIndex: 'revision',
        renderer: this.renderRevision,
        width: 20,
        scope: this
      },{
        id: 'code', 
        dataIndex: 'code',
        renderer: this.renderCode,
        width: 400
      }]
    });
    
    var config = {
      hideHeaders: true,
      autoExpandColumn: 'code',
      store: blameStore,
      colModel: blameColModel,
      stripeRows: false,
      autoHeight: true,
      viewConfig: {
        forceFit: true
      },
      listeners: {
        click: {
          fn: this.onClick,
          scope: this
        }
      }
    }
    
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.repository.BlamePanel.superclass.initComponent.apply(this, arguments);
  },
  
  onClick: function(e){
    var el = e.getTarget('.blame-link');
    if ( el != null ){
      var revision = el.rel;
      if (debug){
        console.debug('load content for ' + revision);
      }
      this.openContentPanel(revision)
    }
  },
  
  openContentPanel: function(revision){
    var id = Sonia.repository.createContentId(
      this.repository, 
      this.path, 
      revision
    );
    main.addTab({
      id: id,
      xtype: 'contentPanel',
      repository: this.repository,
      revision: revision,
      path: this.path,
      closable: true,
      autoScroll: true
    });
  },
  
  renderRevision: function(value, metadata, record){
    var title = 'Revision: ' + value;
    var tip = 'Author: ' + record.get('author').name;
    var when = record.get('when');
    if ( when ){
      tip += '<br />When: ' + Ext.util.Format.formatTimestamp(when);
    }
    var description = record.get('description');
    if (description){
      tip += '<br />Description: ' + description;
    }
    metadata.attr = 'ext:qtitle="' + title + '"' + ' ext:qtip="' + tip + '"';
    return String.format(
      this.linkTemplate,
      Ext.util.Format.ellipsis(value, 10),
      value
    );
  },
  
  renderCode: function(value){
    return '<pre>' + Ext.util.Format.htmlEncode(value) + '</pre>';
  }
  
});

Ext.reg('blamePanel', Sonia.repository.BlamePanel);
