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

Sonia.plugin.StoreInstance = null;

Sonia.plugin.Center = Ext.extend(Ext.util.Observable, {

  waitTitleText: 'Please wait',
  errorTitleText: 'Error',
  restartText: 'Restart the applicationserver to activate the plugin.',
  
  installWaitMsgText: 'Installing Plugin.',
  installSuccessText: 'Plugin successfully installed',
  installFailedText: 'Plugin installation failed',

  uninstallWaitMsgText: 'Uninstalling Plugin.',
  uninstallSuccessText: 'Plugin successfully uninstalled',
  uninstallFailedText: 'Plugin uninstallation failed',

  updateWaitMsgText: 'Updating Plugin.',
  updateSuccessText: 'Plugin successfully updated',
  updateFailedText: 'Plugin update failed',

  constructor : function(config) {
    this.addEvents('install', 'uninstalled', 'updated');
    Sonia.plugin.Center.superclass.constructor.call(this, config);
  },

  getPluginId: function(data){
    return data.groupId + ':' + data.artifactId + ':' + data.version;
  },

  createLoadingBox: function(msg){
    return Ext.MessageBox.show({
      title: this.waitTitleText,
      msg: msg,
      width: 300,
      wait: true,
      animate: true,
      progress: true,
      closable: false
    });
  },

  install: function(pluginId){
    if ( debug ){
      console.debug( 'install plugin ' + pluginId );
    }

    var loadingBox = this.createLoadingBox( this.installWaitMsgText );

    Ext.Ajax.request({
      url: restUrl + 'plugins/install/' + pluginId + '.json',
      method: 'POST',
      scope: this,
      success: function(){
        if ( debug ){
          console.debug('plugin successfully installed');
        }
        loadingBox.hide();
        Ext.MessageBox.alert(this.installSuccessText,
          this.restartText);
        Sonia.plugin.StoreInstance.reload();
        this.fireEvent('installed', pluginId);
      },
      failure: function(){
        if ( debug ){
          console.debug('plugin installation failed');
        }
        loadingBox.hide();
        Ext.MessageBox.show({
          title: this.errorTitleText,
          msg: this.installFailedText,
          buttons: Ext.MessageBox.OK,
          icon:Ext.MessageBox.ERROR
        });
      }
    });
  },

  uninstall: function(pluginId){
    if ( debug ){
      console.debug( 'uninstall plugin ' + pluginId );
    }

    var loadingBox = this.createLoadingBox( this.uninstallWaitMsgText );

    Ext.Ajax.request({
      url: restUrl + 'plugins/uninstall/' + pluginId + '.json',
      method: 'POST',
      scope: this,
      success: function(){
        if ( debug ){
          console.debug('plugin successfully uninstalled');
        }
        loadingBox.hide();
        Ext.MessageBox.alert(this.uninstallSuccessText,
          this.restartText);
        Sonia.plugin.StoreInstance.reload();
        this.fireEvent('uninstalled', pluginId);
      },
      failure: function(){
        if ( debug ){
          console.debug('plugin uninstallation failed');
        }
        loadingBox.hide();
        Ext.MessageBox.show({
          title: this.errorTitleText,
          msg: this.uninstallFailedText,
          buttons: Ext.MessageBox.OK,
          icon:Ext.MessageBox.ERROR
        });
      }
    });
  },

  update: function(pluginId){
    if ( debug ){
      console.debug( 'update plugin to ' + pluginId );
    }

    var loadingBox = createLoadingBox( this.updateWaitMsgText );

    Ext.Ajax.request({
      url: restUrl + 'plugins/update/' + pluginId + '.json',
      method: 'POST',
      scope: this,
      success: function(){
        if ( debug ){
          console.debug('plugin successfully updated');
        }
        loadingBox.hide();
        Ext.MessageBox.alert(this.updateSuccessText,
          this.restartText);
        Sonia.plugin.StoreInstance.reload();
        this.fireEvent('updated', pluginId);
      },
      failure: function(){
        if ( debug ){
          console.debug('plugin update failed');
        }
        loadingBox.hide();
        Ext.MessageBox.show({
          title: this.errorTitleText,
          msg: this.updateFailedText,
          buttons: Ext.MessageBox.OK,
          icon:Ext.MessageBox.ERROR
        });
      }
    });
  }

});

// the plugin center
Sonia.plugin.CenterInstance = new Sonia.plugin.Center();

// plugin grid

Sonia.plugin.Grid = Ext.extend(Sonia.rest.Grid, {

  colNameText: 'Name',
  colAuthorText: 'Author',
  colDescriptionText: 'Description',
  colVersionText: 'Version',
  colStateText: 'State',
  colActionText: 'Action',
  colUrlText: 'Url',
  emptyText: 'No plugins avaiable',

  actionLinkTemplate: '<a style="cursor: pointer;" onclick="{1}">{0}</a>',

  initComponent: function(){

    Sonia.plugin.StoreInstance = new Sonia.plugin.Store({
      url: restUrl + 'plugins/overview.json'
    });


    var pluginColModel = new Ext.grid.ColumnModel({
      defaults: {
        sortable: true,
        scope: this,
        width: 125
      },
      columns: [
        {id: 'name', header: this.colNameText, dataIndex: 'name'},
        {id: 'author', header: this.colAuthorText, dataIndex: 'author'},
        {id: 'description', header: this.colDescriptionText, dataIndex: 'description'},
        {id: 'version', header: this.colVersionText, dataIndex: 'version'},
        {id: 'state', header: this.colStateText, dataIndex: 'state', width: 80},
        {id: 'action', header: this.colActionText, renderer: this.renderActionColumn},
        {id: 'Url', header: this.colUrlText, dataIndex: 'url', renderer: this.renderUrl, width: 150}
      ]
    });

    var config = {
      autoExpandColumn: 'description',
      store: Sonia.plugin.StoreInstance,
      colModel: pluginColModel,
      emptyText: this.emptyText
    };


    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.plugin.Grid.superclass.initComponent.apply(this, arguments);
  },

  renderActionColumn: function(val, meta, record){
    var out = "";
    var data = record.data;
    var id = Sonia.plugin.CenterInstance.getPluginId(data);
    if ( data.state == 'AVAILABLE' ){
      out = String.format(this.actionLinkTemplate, 'Install', 'Sonia.plugin.CenterInstance.install(\'' + id + '\')');
    } else if ( data.state == 'INSTALLED' ){
      out = String.format(this.actionLinkTemplate, 'Uninstall', 'Sonia.plugin.CenterInstance.uninstall(\'' + id + '\')');
    } else if ( data.state == 'UPDATE_AVAILABLE' ){
      out = String.format(this.actionLinkTemplate, 'Update', 'Sonia.plugin.CenterInstance.update(\'' + id + '\')');
      out += ', '
      out += String.format(this.actionLinkTemplate, 'Uninstall', 'Sonia.plugin.CenterInstance.uninstall(\'' + id + '\')');
    }
    return out;
  }
  
});

// register xtype
Ext.reg('pluginGrid', Sonia.plugin.Grid);
