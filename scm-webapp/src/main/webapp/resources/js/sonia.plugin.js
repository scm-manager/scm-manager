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
      fields: [  'name', 'author', 'description', 'url', 'version', 'state', 'groupId', 'artifactId' ],
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


Sonia.plugin.installPlugin = function(pluginId){
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
    url: restUrl + 'plugins/install/' + pluginId + '.json',
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
      loadingBox.hide();
      Ext.MessageBox.show({
        title: 'Error',
        msg: 'Plugin installation failed',
        buttons: Ext.MessageBox.OK,
        icon:Ext.MessageBox.ERROR
      });
    }
  });
}

Sonia.plugin.uninstallPlugin = function(pluginId){
  if ( debug ){
    console.debug( 'uninstall plugin ' + pluginId );
  }

  var loadingBox = Ext.MessageBox.show({
      title: 'Please wait',
      msg: 'Uninstalling Plugin.',
      width: 300,
      wait: true,
      animate: true,
      progress: true,
      closable: false
  });

  Ext.Ajax.request({
    url: restUrl + 'plugins/uninstall/' + pluginId + '.json',
    method: 'POST',
    scope: this,
    success: function(){
      if ( debug ){
        console.debug('plugin successfully uninstalled');
      }
      loadingBox.hide();
      Ext.MessageBox.alert('Plugin successfully uninstalled',
        'Restart the applicationserver to activate the changes.');
    },
    failure: function(){
      if ( debug ){
        console.debug('plugin uninstallation failed');
      }
      loadingBox.hide();
      Ext.MessageBox.show({
        title: 'Error',
        msg: 'Plugin uninstallation failed',
        buttons: Ext.MessageBox.OK,
        icon:Ext.MessageBox.ERROR
      });
    }
  });
}

Sonia.plugin.updatePlugin = function(pluginId){
  if ( debug ){
    console.debug( 'update plugin to ' + pluginId );
  }

  var loadingBox = Ext.MessageBox.show({
      title: 'Please wait',
      msg: 'Update Plugin.',
      width: 300,
      wait: true,
      animate: true,
      progress: true,
      closable: false
  });

  Ext.Ajax.request({
    url: restUrl + 'plugins/update/' + pluginId + '.json',
    method: 'POST',
    scope: this,
    success: function(){
      if ( debug ){
        console.debug('plugin successfully updated');
      }
      loadingBox.hide();
      Ext.MessageBox.alert('Plugin successfully updated',
        'Restart the applicationserver to activate the changes.');
    },
    failure: function(){
      if ( debug ){
        console.debug('plugin update failed');
      }
      loadingBox.hide();
      Ext.MessageBox.show({
        title: 'Error',
        msg: 'Plugin update failed',
        buttons: Ext.MessageBox.OK,
        icon:Ext.MessageBox.ERROR
      });
    }
  });
}

// plugin grid

Sonia.plugin.Grid = Ext.extend(Sonia.rest.Grid, {

  actionLinkTemplate: '<a style="cursor: pointer;" onclick="{1}">{0}</a>',

  initComponent: function(){

    var pluginStore = new Sonia.plugin.Store({
      url: restUrl + 'plugins.json'
    });

    var pluginColModel = new Ext.grid.ColumnModel({
      defaults: {
        sortable: true,
        scope: this,
        width: 125
      },
      columns: [
        {id: 'name', header: 'Name', dataIndex: 'name'},
        {id: 'author', header: 'Author', dataIndex: 'author'},
        {id: 'description', header: 'Description', dataIndex: 'description'},
        {id: 'version', header: 'Version', dataIndex: 'version'},
        {id: 'state', header: 'State', dataIndex: 'state', width: 80},
        {id: 'action', header: 'Action', renderer: this.renderActionColumn},
        {id: 'Url', header: 'Url', dataIndex: 'url', renderer: this.renderUrl, width: 150}
      ]
    });

    var config = {
      autoExpandColumn: 'description',
      store: pluginStore,
      colModel: pluginColModel,
      emptyText: 'No plugins avaiable'
    };


    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.plugin.Grid.superclass.initComponent.apply(this, arguments);
  },

  renderActionColumn: function(val, meta, record){
    var out = "";
    var data = record.data;
    var id = Sonia.plugin.GetPluginId(data);
    if ( data.state == 'AVAILABLE' ){
      out = String.format(this.actionLinkTemplate, 'Install', 'Sonia.plugin.installPlugin(\'' + id + '\')');
    } else if ( data.state == 'INSTALLED' ){
      out = String.format(this.actionLinkTemplate, 'Uninstall', 'Sonia.plugin.uninstallPlugin(\'' + id + '\')');
    } else if ( data.state == 'UPDATE_AVAILABLE' ){
      out = String.format(this.actionLinkTemplate, 'Update', 'Sonia.plugin.updatePlugin(\'' + id + '\')');
      out += ', '
      out += String.format(this.actionLinkTemplate, 'Uninstall', 'Sonia.plugin.uninstallPlugin(\'' + id + '\')');
    }
    return out;
  }
  
});

// register xtype
Ext.reg('pluginGrid', Sonia.plugin.Grid);
