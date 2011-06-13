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

Sonia.repository.RepositoryBrowser = Ext.extend(Ext.grid.GridPanel, {
  
  repository: null,
  
  // TODO i18n
  repositoryBrowserTitleText: 'RepositoryBrowser: {0}',
  iconFolder: 'resources/images/folder.gif',
  iconDocument: 'resources/images/document.gif',
  templateIcon: '<img src="{0}" alt="{1}" title="{2}" />',
  templateLink: '<a class="scm-browser" rel="{1}" href="#">{0}</a>',

  initComponent: function(){
    
    var browserStore = new Sonia.rest.JsonStore({
      proxy: new Ext.data.HttpProxy({
        url: restUrl + 'repositories/' + this.repository.id  + '/browse.json',
        method: 'GET'
      }),
      fields: ['path', 'name', 'length', 'lastModified', 'directory', 'description'],
      root: 'file.children',
      idProperty: 'path',
      autoLoad: true,
      autoDestroy: true,
      listeners: {
        load: {
          fn: this.loadStore,
          scope: this
        }
      }
    });
    
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
        header: 'Name',
        renderer: this.renderName,
        scope: this,
        width: 180
      },{
        id: 'length',
        dataIndex: 'length',
        header: 'Length',
        renderer: this.renderLength
      },{
        id: 'lastModified',
        dataIndex: 'lastModified',
        header: 'Last Modified',
        renderer: Ext.util.Format.formatTimestamp
      },{
        id: 'description',
        dataIndex: 'description',
        header: 'Description'
      }]
    });
    
    var config = {
      autoExpandColumn: 'description',
      title: String.format(this.repositoryBrowserTitleText, this.repository.name),
      store: browserStore,
      colModel: browserColModel,
      loadMask: true,
      listeners: {
        click: {
          fn: this.onClick,
          scope: this
        }
      }
    };
    
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.repository.RepositoryBrowser.superclass.initComponent.apply(this, arguments);
  },
  
  loadStore: function(store, records, extra){
    var path = extra.params.path;
    if ( path != null && path.length > 0 ){
      
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
    
    if ( el != null ){
    
      var rel = el.rel.split(':');
      var path = rel[1];

      if ( rel[0] == 'dir' ){
        this.changeDirectory(path);
      } else {
        this.openFile(path);
      }
    }
  },
  
  openFile: function(path){
    if ( debug ){
      console.debug( 'open file: ' + path );
    }
  },
  
  changeDirectory: function(path){
    if ( path.substr(-1) === '/' ){
      path = path.substr( 0, path.length - 1 );
    }
    
    if (debug){
      console.debug( 'change directory: ' + path );
    }
    
    this.getStore().load({
      params: {
        path: path
      }
    });
  },
  
  renderName: function(name, p, record){
    var path = record.data.directory ? 'dir:' : 'file:';
    path += record.data.path;
    return String.format(this.templateLink, name, path);
  },
  
  renderIcon: function(directory, p, record){
    var icon = null;
    var name = record.data.name;
    if ( directory ){
      icon = this.iconFolder;
    } else {
      icon = this.iconDocument;
    }
    return String.format(this.templateIcon, icon, name, name);
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
