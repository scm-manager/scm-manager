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
Sonia.repository.RepositoryBrowser = Ext.extend(Ext.grid.GridPanel, {
  
  repository: null,
  revision: null,
  path: null,
  
  repositoryBrowserTitleText: 'Source {0}',

  colNameText: 'Name',
  colLengthText: 'Length',
  colLastModifiedText: 'Last Modified',
  colDescriptionText: 'Description',
  
  iconFolder: 'resources/images/folder.png',
  iconDocument: 'resources/images/document.png',
  iconSubRepository: 'resources/images/folder-remote.png',
  templateIcon: '<img src="{0}" alt="{1}" title="{2}" />',
  templateInternalLink: '<a class="scm-browser" rel="{1}">{0}</a>',
  templateExternalLink: '<a class="scm-browser" href="{1}" target="_blank">{0}</a>',
  
  emptyText: 'This directory is empty',

  initComponent: function(){
    
    if (debug){
      console.debug('create new browser for repository ' + this.repository.name + " and revision " + this.revision);
    }
    
    var browserStore = new Sonia.rest.JsonStore({
      proxy: new Ext.data.HttpProxy({
        url: restUrl + 'repositories/' + this.repository.id  + '/browse.json',
        method: 'GET'
      }),
      fields: ['path', 'name', 'length', 'lastModified', 'directory', 'description', 'subrepository'],
      root: 'files',
      idProperty: 'path',
      autoLoad: true,
      autoDestroy: true
    });
    
    // register listener
    browserStore.addListener('load', this.loadStore, this);
    
    if ( this.revision ){
      browserStore.baseParams = {
        revision: this.revision
      };
    }
    
    if ( this.path ){
      browserStore.baseParams = {
        path: this.path
      };
    }
    
    var browserColModel = new Ext.grid.ColumnModel({
      defaults: {
        sortable: false
      },
      columns: [{
        id: 'icon',
        dataIndex: 'directory',
        header: '',
        width: 28,
        renderer: this.renderIcon,
        scope: this
      },{
        id: 'name',
        dataIndex: 'name',
        header: this.colNameText,
        renderer: this.renderName,
        scope: this,
        width: 180
      },{
        id: 'length',
        dataIndex: 'length',
        header: this.colLengthText,
        renderer: this.renderLength
      },{
        id: 'lastModified',
        dataIndex: 'lastModified',
        header: this.colLastModifiedText,
        renderer: Ext.util.Format.formatTimestamp
      },{
        id: 'description',
        dataIndex: 'description',
        header: this.colDescriptionText
      },{
        id: 'subrepository',
        dataIndex: 'subrepository',
        hidden: true
      }]
    });
    
    var bar = [this.createFolderButton('', '')];
    this.appendRepositoryProperties(bar);
    
    var config = {
      bbar: bar,
      autoExpandColumn: 'description',
      title: String.format(this.repositoryBrowserTitleText, this.repository.name),
      store: browserStore,
      colModel: browserColModel,
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
    };
    
    config.tbar = this.createTopToolbar();
    
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.repository.RepositoryBrowser.superclass.initComponent.apply(this, arguments);
  },
  
  createTopToolbar: function(){
    var items = [this.repository.name];
    
    var type = Sonia.repository.getTypeByName( this.repository.type );
    var branches = false;
    if (type && type.supportedCommands && type.supportedCommands.indexOf('BRANCHES') >= 0){
      
      branches = true;
      
      var branchStore = new Sonia.rest.JsonStore({
        proxy: new Ext.data.HttpProxy({
          url: restUrl + 'repositories/' + this.repository.id + '/branches.json',
          method: 'GET',
          disableCaching: false
        }),
        root: 'branch',
        idProperty: 'name',
        fields: [ 'name', 'revision' ]
      });

      items.push('->','Branches:', ' ',{
        xtype: 'combo',
        valueField: 'revision',
        displayField: 'name',
        typeAhead: false,
        editable: false,
        triggerAction: 'all',
        store: branchStore,
        listeners: {
          select: {
            fn: this.selectRev,
            scope: this
          }
        }
      });
      
    }
   
    if ( type && type.supportedCommands && type.supportedCommands.indexOf('TAGS') >= 0){
    
      var tagStore = new Sonia.rest.JsonStore({
        proxy: new Ext.data.HttpProxy({
          url: restUrl + 'repositories/' + this.repository.id + '/tags.json',
          method: 'GET',
          disableCaching: false
        }),
        root: 'tag',
        idProperty: 'name',
        fields: [ 'name', 'revision' ]
      });

      if (branches){
        items.push(' ');
      } else {
        items.push('->');
      }

      items.push('Tags:', ' ',{
        xtype: 'combo',
        valueField: 'revision',
        displayField: 'name',
        typeAhead: false,
        editable: false,
        triggerAction: 'all',
        store: tagStore,
        listeners: {
          select: {
            fn: this.selectRev,
            scope: this
          }
        }
      });
    
    }
    
    var tbar = {
      xtype: 'toolbar',
      items: [items]
    };
    
    return tbar;
  },
  
  selectRev: function(combo, rec){
    var tag = rec.get('name');
    if (debug){
      console.debug('select rev ' + tag);
    }
    this.revision = rec.get('revision');
    this.getStore().load({
      params: {
        revision: this.revision,
        path: this.path
      }
    });
    
    this.reRenderBottomBar(this.path);
    this.updateHistory();
  },
  
  loadStore: function(store, records, extra){
    var path = extra.params.path;
    if ( path && path.length > 0 ){
      
      var index = path.lastIndexOf('/');
      if ( index > 0 ){
        path = path.substr(0, index);
      } else {
        path = '';
      }
      
      var File = Ext.data.Record.create([{
          name: 'name'
        },{
          name: 'path'
        },{
          name: 'directory'
        },{
          name: 'length'
        },{
          name: 'lastModified'
      }]);
    
      store.insert(0, new File({
        name: '..',
        path: path,
        directory: true,
        length: 0
      }));
    }
  },
  
  onClick: function(e){
    var el = e.getTarget('.scm-browser');
    
    if ( el ){
    
      var rel = el.rel;
      var index = rel.indexOf(':');
      if ( index > 0 ){
        var prefix = rel.substring(0, index);
        var path = rel.substring(index + 1);
        
        if ( prefix === 'sub' ){
          this.openSubRepository(path);
        } else if ( prefix === 'dir' ){
          this.changeDirectory(path);
        } else {
          this.openFile(path);
        }
      }
    }
  },
  
  openSubRepository: function(subRepository){
    if (debug){
      console.debug('open sub repository ' + subRepository);
    }
    var revision = null;
    var index = subRepository.indexOf(';');
    if ( index > 0 ){
      revision = subRepository.substring(index + 1);
      subRepository = subRepository.substring(0, index);
    }
    var id = 'repositoryBrowser;' + subRepository + ';' + revision + ';null';
    Sonia.repository.get(subRepository, function(repository){
      var panel = Ext.getCmp(id);
      if (! panel){
        panel = {
          id: id,
          xtype: 'repositoryBrowser',
          repository : repository,
          revision: revision,
          closable: true,
          autoScroll: true
        };
      }
      main.addTab(panel);
    });
  },
  
  appendRepositoryProperties: function(bar){
    bar.push('->',this.repository.name);
    if ( this.revision ){
      bar.push(': ', this.revision);
    }
  },
  
  openFile: function(path){
    if ( debug ){
      console.debug( 'open file: ' + path );
    }
    
    var id = Sonia.repository.createContentId(
      this.repository, 
      path, 
      this.revision
    );
    
    main.addTab({
      id: id,
      path: path,
      revision: this.revision,
      repository: this.repository,
      xtype: 'contentPanel',
      closable: true,
      autoScroll: true
    });
  },
  
  changeDirectory: function(path){
    if ( path.substr(-1) === '/' ){
      path = path.substr( 0, path.length - 1 );
    }
    
    if (debug){
      console.debug( 'change directory: ' + path );
    }
    
    var params = {
      path: path
    };
    
    if (this.revision){
      params.revision = this.revision;
    }
    
    this.getStore().load({
      params: params
    });
    
    this.path = path;
    this.updateHistory();
    
    this.reRenderBottomBar(path);
  },
  
  updateHistory: function(){
    var token = Sonia.History.createToken(
      'repositoryBrowser', 
      this.repository.id,
      this.revision,
      this.path
    );
    Sonia.History.add(token);
  },
  
  createFolderButton: function(path, name){
    return {
      xtype: 'button',
      icon: this.iconFolder,
      text: name + '/',
      handler: this.changeDirectory.createDelegate(this, [path])
    };
  },
  
  renderClickPath: function(path){
    this.reRenderBottomBar(path);
  },
  
  reRenderBottomBar: function(path){
    var bbar = this.getBottomToolbar();
    bbar.removeAll();
    
    var parts = path.split('/');
    var currentPath = '';
    var items = [this.createFolderButton(currentPath, '')];
          
    if ( path !== '' ){
      for (var i=0; i<parts.length; i++){
        currentPath += parts[i] + '/';
        items.push(this.createFolderButton(currentPath, parts[i]));
      }
    }
    
    items.push('->', this.repository.name);
    if ( this.revision ){
      items.push(':', this.revision);
    }
    
    bbar.add(items);
    bbar.doLayout();
  },
  
  getRepositoryPath: function(url){
    var ctxPath = Sonia.util.getContextPath();
    var i = url.indexOf(ctxPath);
    if ( i > 0 ){
      url = url.substring( i + ctxPath.length );
    }
    if ( url.indexOf('/') === 0 ){
      url = url.substring(1);
    }
    return url;
  },
  
  transformLink: function(url, revision){
    var link = null;
    var server = Sonia.util.getServername(url);
    if ( server === window.location.hostname || server === 'localhost' ){
      var repositoryPath = this.getRepositoryPath( url );
      if (repositoryPath){
        link = 'sub:' + repositoryPath;
        if (revision){
          link += ';' + revision;
        }
      }
    }
    return link;
  },
  
  renderLink: function(text, record){
    var subRepository = record.get('subrepository');
    var folder = record.get('directory');
    var path = null;
    var template = null;
    if ( subRepository ){
      // get real revision (.hgsubstate)?
      var subRepositoryUrl = subRepository['browser-url'];
      if (!subRepositoryUrl){
        subRepositoryUrl = subRepository['repository-url'];
      }
      path = this.transformLink(subRepositoryUrl, subRepository['revision']);
      if ( path ){
        template = this.templateInternalLink;
      } else {
        path = subRepositoryUrl;
        template = this.templateExternalLink;
      }
    } else {
      if (folder){
        path = 'dir:';
      } else {
        path = 'file:';
      }
      path += record.get('path');
      template = this.templateInternalLink;
    }
    
    return String.format(template, text, path);    
  },
  
  renderName: function(name, p, record){
    return this.renderLink(name, record);
  },
  
  renderIcon: function(directory, p, record){
    var icon = null;
    var subRepository = record.get('subrepository');
    
    var name = record.get('name');
    if ( subRepository ){
      icon = this.iconSubRepository;
    } else if (directory){
      icon = this.iconFolder;
    } else {
      icon = this.iconDocument;
    }
    var img = String.format(this.templateIcon, icon, name, name);
    return this.renderLink(img, record);
  },
  
  renderLength: function(length, p, record){
    var result = '';
    var directory = record.data.directory;
    if ( ! directory ){
      result = Ext.util.Format.fileSize(length);
    }
    return result;
  }

});

// register xtype
Ext.reg('repositoryBrowser', Sonia.repository.RepositoryBrowser);


// register history handler
Sonia.History.register('repositoryBrowser', {
  
  onActivate: function(panel){
    return Sonia.History.createToken(
      'repositoryBrowser', 
      panel.repository.id,
      panel.revision,
      panel.path
    );
  },
  
  onChange: function(repoId, revision, path){
    if (revision === 'null'){
      revision = null;
    }
    if (path === 'null'){
      path = '';
    }
    var id = 'repositoryBrowser;' + repoId + ';' + revision;
    Sonia.repository.get(repoId, function(repository){
      var panel = Ext.getCmp(id);
      if (! panel){
        panel = {
          id: id,
          xtype: 'repositoryBrowser',
          repository : repository,
          revision: revision,
          closable: true,
          autoScroll: true
        };
        if (path){
          panel.path = path;
        }
      } else {
        panel.changeDirectory(path);
      }
      main.addTab(panel);
    });
  }
});