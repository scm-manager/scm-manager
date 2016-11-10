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
Sonia.repository.Grid = Ext.extend(Sonia.rest.Grid, {

  // templates
  typeIconTemplate: '<img src="{0}" alt="{1}" title="{2}">',

  colNameText: 'Name',
  colTypeText: 'Type',
  colContactText: 'Contact',
  colDescriptionText: 'Description',
  colCreationDateText: 'Creation date',
  colLastModifiedText: 'Last modified',
  colUrlText: 'Url',
  colArchiveText: 'Archive',
  emptyText: 'No repository is configured',
  formTitleText: 'Repository Form',
  unknownType: 'Unknown',
  
  archiveIcon: 'resources/images/archive.png',
  warningIcon: 'resources/images/warning.png',
  
  filterRequest: null,
  
  /**
   * @deprecated use filterRequest
   */
  searchValue: null,
  
  /**
   * @deprecated use filterRequest
   */
  typeFilter: null,
  
  // TODO find better text
  mainGroup: 'main',
  
  // for history
  parentPanel: null,
  ready: false,

  initComponent: function(){

    var repositoryStore = new Ext.data.GroupingStore({
      proxy: new Ext.data.HttpProxy({
        url: restUrl + 'repositories.json',
        disableCaching: false
      }),
      idProperty: 'id',
      reader: new Ext.data.JsonReader({
        fields: [{
          name: 'id'
        },{
          name: 'group',
          convert: this.convertToGroup
        },{
          name: 'name',
          sortType:'asUCString'
        },{
          name: 'type'
        },{
          name: 'contact'
        },{
          name: 'description'
        },{
          name: 'creationDate'
        },{
          name: 'lastModified'
        },{
          name: 'public'
        },{
          name:'permissions'
        },{
          name: 'properties'
        },{
          name: 'archived'
        },{
          name: 'healthCheckFailures',
          defaultValue: null
        }]
      }),
      sortInfo: {
        field: 'name'
      },
      autoDestroy: true,
      autoLoad: true,
      remoteGroup: false,
      groupOnSort: false,
      groupField: 'group',
      groupDir: 'AES',
      listeners: {
        load: {
          fn: this.storeLoad,
          scope: this
        },
        exception: Sonia.rest.ExceptionHandler
      }
    });

    var repositoryColModel = new Ext.grid.ColumnModel({
      defaults: {
        sortable: true,
        scope: this,
        width: 125
      },
      columns: [{
        id: 'iconType',
        dataIndex: 'type',
        renderer: this.renderTypeIcon,
        width: 20
      },{
        id: 'name', 
        header: this.colNameText, 
        dataIndex: 'name', 
        renderer: this.renderName,
        scope: this
      },{
        id: 'type', 
        header: this.colTypeText, 
        dataIndex: 'type', 
        renderer: this.renderRepositoryType, 
        width: 80,
        hidden: true
      },{
        id: 'contact', 
        header: this.colContactText, 
        dataIndex: 'contact', 
        renderer: this.renderMailto
      },{
        id: 'description', 
        header: this.colDescriptionText, 
        dataIndex: 'description'
      },{
        id: 'creationDate', 
        header: this.colCreationDateText, 
        dataIndex: 'creationDate', 
        renderer: Ext.util.Format.formatTimestamp
      },{
        id: 'lastModified', 
        header: this.colLastModifiedText, 
        dataIndex: 'lastModified', 
        renderer: Ext.util.Format.formatTimestamp,
        hidden: true
      },{
        id: 'Url', 
        header: this.colUrlText, 
        dataIndex: 'name', 
        renderer: this.renderRepositoryUrl, 
        scope: this,
        width: 250
      },{
        id: 'Archive', 
        header: this.colArchiveText, 
        dataIndex: 'archived', 
        width: 40,
        hidden: ! state.clientConfig.enableRepositoryArchive,
        renderer: this.renderArchive,
        scope: this
      },{
        id: 'group', 
        dataIndex: 'group', 
        hidden: true,
        hideable: false,
        groupRenderer: this.renderGroupName, 
        scope: this
      }]
    });

    if (debug){
      var msg = "grouping is ";
      if ( state.clientConfig.disableGroupingGrid ){
        msg += "disabled";
      } else {
        msg += "enabled";
      }
      console.debug( msg );
    }
    
    if ( state.clientConfig.enableRepositoryArchive ){
      if ( !this.filterRequest ){
        this.filterRequest = {};
      }
      this.filterRequest.archived = false;
    }

    var config = {
      autoExpandColumn: 'description',
      store: repositoryStore,
      colModel: repositoryColModel,
      emptyText: this.emptyText,
      listeners: {
        fallBelowMinHeight: {
          fn: this.onFallBelowMinHeight,
          scope: this
        }
      },
      view: new Ext.grid.GroupingView({
        idPrefix: '{grid.id}',
        enableGrouping: ! state.clientConfig.disableGroupingGrid,
        enableNoGroups: false,
        forceFit: true,
        groupMode: 'value',
        enableGroupingMenu: false,
        groupTextTpl: '{group} ({[values.rs.length]} {[values.rs.length > 1 ? "Repositories" : "Repository"]})',
        getRowClass: function(record){
          var rowClass = '';
          var healthFailures = record.get('healthCheckFailures');
          if (healthFailures && healthFailures.length > 0){
            rowClass = 'unhealthy';
          }
          return rowClass;
        }
      })
    };
    
    this.addEvents('repositorySelected');

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.repository.Grid.superclass.initComponent.apply(this, arguments);
    
    if (this.parentPanel){
      this.parentPanel.repositoryGrid = this;
    }
  },
  
  renderTypeIcon: function(type, meta, record){
    var result;
    if ( record ){
      var healthFailures = record.get('healthCheckFailures');
      if (healthFailures && healthFailures.length > 0){
         result = String.format(this.typeIconTemplate, this.warningIcon, type, type);
      }
    }
    if (!result){
      result = this.getTypeIcon(type);
    }
    return result;
  },
  
  getTypeIcon: function(type){
    var result = '';
    var icon = Sonia.repository.getTypeIcon(type);
    if ( icon ){
      var displayName = type;
      var t = Sonia.repository.getTypeByName(type);
      if (t){
        displayName = t.displayName;
      }
      result = String.format(this.typeIconTemplate, icon, type, displayName);
    }
    return result;
  },
  
  renderRepositoryUrl: function(name, meta, record){
    var type = record.get('type');
    return this.renderUrl(
      Sonia.repository.createUrl(type, name)
    );
  },
  
  renderArchive: function(v){
    return v ? '<img src=' + this.archiveIcon + ' alt=' + v + '>' : '';
  },
  
  convertToGroup: function(v, data){
    var name = data.name;
    var i = name.lastIndexOf('/');
    if ( i > 0 ){
      name = name.substring(0, i);
    } else {
      name = "zzz__";
    }
    return name;
  },
  
  renderName: function(v, meta, record, rowIndex, colIndex, store){
    // TODO check if grouping is enabled
    var i = v.lastIndexOf('/');
    if ( i > 0 ){
      v = v.substring(i+1);
    }
    return v;
  },
  
  renderGroupName: function(v){
    if (v === 'zzz__'){
      v = this.mainGroup;
    }
    return v;
  },
  
  storeLoad: function(){
    if (this.searchValue){
      this.filterStore();
    }
    if (this.filterRequest){
      this.filterByRequest();
    }
    this.ready = true;
  },

  onFallBelowMinHeight: function(height, minHeight){
    var p = Ext.getCmp('repositoryEditPanel');
    this.setHeight(minHeight);
    var epHeight = p.getHeight();
    p.setHeight(epHeight - (minHeight - height));
    // rerender
    this.doLayout();
    p.doLayout();
    this.ownerCt.doLayout();
  },
  
  getFilterRequest: function(){
    if ( ! this.filterRequest ){
      this.filterRequest = {};
    }
    return this.filterRequest;
  },
  
  /**
   * @deprecated use filterByRequest
   */
  search: function(value){
    this.searchValue = value;
    this.filterStore();
  },

  /**
   * @deprecated use filterByRequest
   */
  filter: function(type){
    this.typeFilter = type;
    this.filterStore();
  },
  
  clearStoreFilter: function(){
    this.filterRequest = null;
    this.searchValue = null;
    this.typeFilter = null;
    this.getStore().clearFilter();
  },
  
  filterByRequest: function(){
    if (debug){
      console.debug('filter repository store by request:');
      console.debug(this.filterRequest);
    }
    var store = this.getStore();
    if ( ! this.filterRequest ){
      store.clearFilter();
    } else {
      var query = this.filterRequest.query;
      if ( query ){
        query = query.toLowerCase();
      }
      var archived = ! state.clientConfig.enableRepositoryArchive || this.filterRequest.archived;
      store.filterBy(function(rec){
        var desc = rec.get('description');
        return (! query || rec.get('name').toLowerCase().indexOf(query) >= 0 ||
               (desc && desc.toLowerCase().indexOf(query) >= 0)) && 
               (! this.filterRequest.type || rec.get('type') === this.filterRequest.type) &&
               (archived || ! rec.get('archived'));
      }, this);
    }
  },
  
  /**
   * @deprecated use filterByRequest
   */
  filterStore: function(){
    var store = this.getStore();
    if ( ! this.searchValue && ! this.typeFilter ){
      store.clearFilter();
    } else {    
      var search = null;
      if ( this.searchValue ){
        search = this.searchValue.toLowerCase();
      }
      store.filterBy(function(rec){
        var desc = rec.get('description');
        return (! search || rec.get('name').toLowerCase().indexOf(search) >= 0 ||
               (desc && desc.toLowerCase().indexOf(search) >= 0)) && 
               (! this.typeFilter || rec.get('type') === this.typeFilter);
      }, this);
    }
  },

  /**
   * TODO move to panel
   */
  selectItem: function(item){
    if ( debug ){
      console.debug( item.name + ' selected' );
    }
    
    if ( this.parentPanel ){
      this.parentPanel.updateHistory(item);
    }
    
    var owner = Sonia.repository.isOwner(item);
    
    this.fireEvent('repositorySelected', item, owner);

    var infoPanel = main.getInfoPanel(item.type);
    infoPanel.item = item;
    
    var settingsForm = main.getSettingsForm(item.type);
    settingsForm.item = item;
    settingsForm.onUpdate = {
      fn: this.reload,
      scope: this
    };
    settingsForm.onCreate = {
      fn: this.reload,
      scope: this
    };
    
    var panels = [infoPanel];
    
    if ( owner ){
      panels.push(
        settingsForm, {
        item: item,
        xtype: 'repositoryPermissionsForm',
        listeners: {
          updated: {
            fn: this.reload,
            scope: this
          },
          created: {
            fn: this.reload,
            scope: this
          }
        }
      });
      
    } else {
      Ext.getCmp('repoRmButton').setDisabled(true);
    }
    
    if (admin && item.healthCheckFailures && item.healthCheckFailures.length > 0){
      panels.push({
        xtype: 'repositoryHealthCheckFailurePanel',
        grid: this,
        repository: item,
        healthCheckFailures: item.healthCheckFailures
      });
    }
    
    // call open listeners
    Ext.each(Sonia.repository.openListeners, function(listener){
      if (Ext.isFunction(listener)){
        listener(item, panels);
      } else {
        listener.call(listener.scope, item, panels);
      }
    });
  
    // get the xtype of the currently opened tab
    // search for the tab with the same xtype and activate it
    // see issue https://goo.gl/3RGnA3
    var activeTab = 0;
    var activeXtype = this.getActiveTabXtype();
    if (activeXtype){
      for (var i=0; i<panels.length; i++){
        if (panels[i].xtype === activeXtype){
          activeTab = i;
          break;
        }
      }
    }

    Sonia.repository.setEditPanel(panels, activeTab);
  },
  
  getActiveTabXtype: function(){
    var type = null;
    var rep = Ext.getCmp('repositoryEditPanel');
    if (rep){
      var at = rep.getActiveTab();
      if (at && at.xtype){
        type = at.xtype;
      }
    }
    return type;
  },

  renderRepositoryType: function(repositoryType){
    var displayName = this.unknownType;
    var rec = repositoryTypeStore.queryBy(function(rec){
      return rec.data.name === repositoryType;
    }).itemAt(0);
    if ( rec ){
      displayName = rec.get('displayName');
    }
    return displayName;
  }
  
});

// register xtype
Ext.reg('repositoryGrid', Sonia.repository.Grid);