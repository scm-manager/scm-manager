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

// register namespace
Ext.ns("Sonia.plugin");

Sonia.plugin.Store = Ext.extend(Sonia.rest.JsonStore, {

  constructor: function(config) {
    var baseConfig = {
      root: 'plugin-information',
      fields: [  'name', 'author', 'description', 'url', 'version', 'groupId', 'artifactId' ],
      sortInfo: {
        field: 'name'
      }
    };
    Sonia.plugin.Store.superclass.constructor.call(this, Ext.apply(config, baseConfig));
  }

});

Sonia.plugin.GetPluginId = function(data){
  return data.groupId + ':' + data.artifactId + ':' + data.version;
}

Sonia.plugin.DefaultColumns = [
  {id: 'name', header: 'Name', dataIndex: 'name'},
  {id: 'author', header: 'Author', dataIndex: 'author'},
  {id: 'description', header: 'Description', dataIndex: 'description'},
  {id: 'version', header: 'Version', dataIndex: 'version'}
]


// installed plugins grid

Sonia.plugin.InstalledGrid = Ext.extend(Sonia.rest.Grid, {

  initComponent: function(){

    var pluginStore = new Sonia.plugin.Store({
      url: restUrl + 'plugins/installed.json'
    });

    var columns = [];
    columns = columns.concat( columns, Sonia.plugin.DefaultColumns );
    columns.push(
      {id: 'Url', header: 'Url', dataIndex: 'url', renderer: this.renderUrl, width: 250}
    );

    var pluginColModel = new Ext.grid.ColumnModel({
      defaults: {
        sortable: true,
        scope: this,
        width: 125
      },
      columns: columns
    });

    var config = {
      autoExpandColumn: 'description',
      store: pluginStore,
      colModel: pluginColModel
    };


    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.plugin.InstalledGrid.superclass.initComponent.apply(this, arguments);
  }

});

// register xtype
Ext.reg('installedPluginsGrid', Sonia.plugin.InstalledGrid);

// loading window

Sonia.plugin.LoadingWindow = Ext.extend(Ext.Window,{

  initComponent: function(){

    var config = {
      layout:'fit',
      width:300,
      height:150,
      closable: false,
      resizable: false,
      plain: true,
      border: false,
      modal: true,
      items: [{
        xtype: 'progress'
      }]
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.login.Window.superclass.initComponent.apply(this, arguments);
  }

});

// available plugins grid

Sonia.plugin.AvailableGrid = Ext.extend(Sonia.rest.Grid,{

  initComponent: function(){

    var pluginStore = new Sonia.plugin.Store({
      url: restUrl + 'plugins/available.json'
    });
    
    var columns = [];
    columns = columns.concat( columns, Sonia.plugin.DefaultColumns );
    columns.push(
      {id: 'Url', header: 'Url', dataIndex: 'url', renderer: this.renderUrl, width: 250},
      {id: 'Install', header: 'Install', renderer: this.renderInstallLink, width: 60}
    );

    var pluginColModel = new Ext.grid.ColumnModel({
      defaults: {
        sortable: true,
        scope: this,
        width: 125
      },
      columns: columns
    });

    var config = {
      autoExpandColumn: 'description',
      store: pluginStore,
      colModel: pluginColModel
    };

    this.on('cellclick', this.cellClick);

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.plugin.AvailableGrid.superclass.initComponent.apply(this, arguments);
  },

  renderInstallLink: function(){
    return '<a style="cursor: pointer">Install</a>'
  },

  cellClick: function(grid, rowIndex, columnIndex, e){
    if(columnIndex==grid.getColumnModel().getIndexById('Install')){
      var record = grid.getStore().getAt(rowIndex);
      this.installPlugin(Sonia.plugin.GetPluginId(record.data));
    }
  },

  installPlugin: function(pluginId){
    if ( debug ){
      console.debug( 'install plugin ' + pluginId );
    }

    var loadingBox = Ext.MessageBox.show({
        title: 'Please wait',
        msg: 'Installing Plugin.',
        width: 300,
        wait: true,
        animate: true,
        progress: true,
        closable: false
    });

    Ext.Ajax.request({
      url: restUrl + 'plugins/available/' + pluginId + '.json',
      method: 'POST',
      scope: this,
      success: function(){
        if ( debug ){
          console.debug('plugin successfully installed');
        }
        loadingBox.hide();
        Ext.MessageBox.alert('Plugin successfully installed',
          'Restart the applicationserver to activate the plugin.');
      },
      failure: function(){
        if ( debug ){
          console.debug('plugin installation failed');
        }
        alert( 'failure' );
        loadingBox.hide();
      }
    });
  }

});

Ext.reg('availablePluginsGrid', Sonia.plugin.AvailableGrid);
