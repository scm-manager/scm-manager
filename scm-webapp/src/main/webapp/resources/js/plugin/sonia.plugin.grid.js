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

// the plugin center
Sonia.plugin.CenterInstance = new Sonia.plugin.Center();


// plugin grid
Sonia.plugin.Grid = Ext.extend(Sonia.rest.Grid, {

  colNameText: 'Name',
  colAuthorText: 'Author',
  colDescriptionText: 'Description',
  colVersionText: 'Version',
  colActionText: 'Action',
  colUrlText: 'Url',
  colCategoryText: 'Category',
  emptyText: 'No plugins avaiable',

  actionLinkTemplate: '<a style="cursor: pointer;" onclick="Sonia.plugin.CenterInstance.{1}(\'{2}\')">{0}</a>',

  initComponent: function(){

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
        {id: 'action', header: this.colActionText, renderer: this.renderActionColumn},
        {id: 'Url', header: this.colUrlText, dataIndex: 'url', renderer: this.renderUrl, width: 150},
        {id: 'Category', header: this.colCategoryText, dataIndex: 'category', hidden: true, hideable: false}
      ]
    });
    
    var pluginStore = new Ext.data.GroupingStore({
      proxy: new Ext.data.HttpProxy({
        url: restUrl + 'plugins/overview.json',
        disableCaching: false
      }),
      reader: new Ext.data.JsonReader({
        fields: [ 'name', 'author', 'description', 'url', 'version', 'state', 'groupId', 'artifactId', {
          name: 'category',
          convert: this.convertCategory,
          scope: this
        }]
      }),
      sortInfo: {
        field: 'name'
      },
      autoLoad: true,
      autoDestroy: true,
      remoteGroup: false,
      groupOnSort: false,
      groupField: 'category',
      groupDir: 'AES'
    });

    var config = {
      title: main.tabPluginsText,
      autoExpandColumn: 'description',
      store: pluginStore,
      colModel: pluginColModel,
      emptyText: this.emptyText,
      view: new Ext.grid.GroupingView({
        forceFit: true,
        enableGroupingMenu: false,
        groupTextTpl: '{group} ({[values.rs.length]} {[values.rs.length > 1 ? "Plugins" : "Plugin"]})'
      })
    };

    Sonia.plugin.CenterInstance.addListener('changed', function(){
      if (debug){
        console.debug( 'receive change event, reload plugin store' );
      }
      this.getStore().reload();
    }, this);

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.plugin.Grid.superclass.initComponent.apply(this, arguments);
  },
  
  convertCategory: function(category){
    return category ? category : 'Miscellaneous';
  },

  renderActionColumn: function(val, meta, record){
    var out = "";
    var data = record.data;
    var id = Sonia.plugin.CenterInstance.getPluginId(data);
    if ( data.state == 'AVAILABLE' ){
      out = String.format(this.actionLinkTemplate, 'Install', 'install', id);
    } else if ( data.state == 'INSTALLED' ){
      out = String.format(this.actionLinkTemplate, 'Uninstall', 'uninstall', id);
    } else if ( data.state == 'UPDATE_AVAILABLE' ){
      out = String.format(this.actionLinkTemplate, 'Update', 'update', id);
      out += ', '
      out += String.format(this.actionLinkTemplate, 'Uninstall', 'uninstall', id);
    }
    return out;
  }
  
});

// register xtype
Ext.reg('pluginGrid', Sonia.plugin.Grid);
