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

// RepositoryPanel
Sonia.repository.Panel = Ext.extend(Sonia.rest.Panel, {

  titleText: 'Repository Form',
  emptyText: 'Add or select an Repository',
  
  // TODO i18n
  archiveText: 'Archive',
  unarchiveText: 'Unarchive',
  archiveTitleText: 'Archive Repository',
  archiveMsgText: 'Archive Repository "{0}"?',
  errorArchiveMsgText: 'Repository  archival failed',

  removeTitleText: 'Remove Repository',
  removeMsgText: 'Remove Repository "{0}"?',
  errorTitleText: 'Error',
  errorMsgText: 'Repository deletion failed',
  
  archiveIcon: 'resources/images/archive.png',
  
  repositoryGrid: null,

  initComponent: function(){

    // create new store for repository types
    var typeStore = new Ext.data.JsonStore({
      id: 1,
      fields: [ 'displayName', 'name' ]
    });
    
    // load types from server state
    typeStore.loadData(state.repositoryTypes);
    
    // add empty value
    var t = new typeStore.recordType({
      displayName: '',
      name: ''
    });
    
    typeStore.insert(0, t);

    var toolbar = [];
    if ( admin ){
      toolbar.push({
        xtype: 'tbbutton', 
        text: this.addText, 
        icon: this.addIcon, 
        scope: this, 
        handler: this.showAddForm
      });
    }
    
    // repository archive
    if (state.clientConfig.enableRepositoryArchive){
      toolbar.push({
        xtype: 'tbbutton', 
        id: 'repoArchiveButton', 
        disabled: true, 
        text: this.archiveText,
        icon: this.archiveIcon,
        scope: this,
        handler: this.toggleArchive        
      });
    }
    
    toolbar.push({
      xtype: 'tbbutton', 
      id: 'repoRmButton', 
      disabled: true, 
      text: this.removeText, 
      icon: this.removeIcon, 
      scope: this,
      handler: this.removeRepository
    },'-', {
      xtype: 'tbbutton', 
      text: this.reloadText, 
      icon: this.reloadIcon, 
      scope: this, 
      handler: this.reload
    },'-',{
      xtype: 'label',
      text: 'Filter: ',
      cls: 'ytb-text'
    }, '  ', {
      id: 'repositoryTypeFilter',
      xtype: 'combo',
      hiddenName : 'type',
      typeAhead: true,
      triggerAction: 'all',
      lazyRender: true,
      mode: 'local',
      editable: false,
      store: typeStore,
      valueField: 'name',
      displayField: 'displayName',
      allowBlank: true,
      listeners: {
        select: {
          fn: this.filterByType,
          scope: this
        }
      },
      tpl:'<tpl for=".">' +
        '<div class="x-combo-list-item">' +
          '{displayName}&nbsp;' +
        '</div></tpl>'
    }, '  ',{
      xtype: 'label',
      text: 'Search: ',
      cls: 'ytb-text'
    }, '  ',{
      id: 'repositorySearch',
      xtype: 'textfield',
      enableKeyEvents: true,
      listeners: {
        keyup: {
          fn: this.search,
          scope: this
        }
      }
    });
    
    // repository archive
    if (state.clientConfig.enableRepositoryArchive){
      toolbar.push('  ',{
        id: 'displayArchived',
        xtype: 'checkbox',
        listeners: {
          check: {
            fn: this.filterByArchived,
            scope: this
          }
        }      
      },{
        xtype: 'label',
        text: 'Archive',
        cls: 'ytb-text'
      })
    }

    var config = {
      tbar: toolbar,
      items: [{
        id: 'repositoryGrid',
        xtype: 'repositoryGrid',
        region: 'center',
        parentPanel: this,
        listeners: {
          repositorySelected: {
            fn: this.onRepositorySelection,
            scope: this
          }
        }
      },{
        id: 'repositoryEditPanel',
        xtype: 'tabpanel',
        activeTab: 0,
        height: 250,
        split: true,
        border: true,
        region: 'south',
        items: [{
          bodyCssClass: 'x-panel-mc',
          title: this.titleText,
          padding: 5,
          html: this.emptyText
        }]
      }]
    }

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.repository.Panel.superclass.initComponent.apply(this, arguments);
  },
  
  getGrid: function(){
    if ( ! this.repositoryGrid ){
      if ( debug ){
        console.debug('repository grid not found, retrive by cmp id');
      }
      this.repositoryGrid = Ext.getCmp('repositoryGrid');
    }
    return this.repositoryGrid;
  },
  
  updateHistory: function(item){
    var token = Sonia.History.createToken('repositoryPanel', item.id);
    Sonia.History.add(token);
  },
  
  filterByArchived: function(checkbox, checked){
    var grid = this.getGrid();
    grid.getFilterRequest().archived = checked;
    grid.filterByRequest();
  },
  
  filterByType: function(combo, rec){
    var grid = this.getGrid();
    grid.getFilterRequest().type = rec.get('name');
    grid.filterByRequest();
  },
  
  search: function(field){
    var grid = this.getGrid();
    grid.getFilterRequest().query = field.getValue();
    grid.filterByRequest();
  },
  
  getSelectedRepository: function(){
    var repository = null;
    var grid = this.getGrid();
    var selected = grid.getSelectionModel().getSelected();
    if ( selected ){
      repository = selected.data;
    } else if (debug) {
      console.debug( 'no repository selected' );
    }
    return repository;
  },
  
  executeRemoteCall: function(title, message, method, url, data, failureCallback){
    Ext.MessageBox.show({
      title: title,
      msg: message,
      buttons: Ext.MessageBox.OKCANCEL,
      icon: Ext.MessageBox.QUESTION,
      fn: function(result){
        if ( result == 'ok' ){

          if ( debug ){
            console.debug('call repository repository action '+ method + ' on ' + url );
          }
          
          var el = this.el;
          var tid = setTimeout( function(){el.mask('Loading ...');}, 100);

          if (data && data.group){
            delete data.group;
          }

          Ext.Ajax.request({
            url: url,
            method: method,
            jsonData: data,
            scope: this,
            success: function(){
              this.reload();
              this.resetPanel();
              clearTimeout(tid);
              el.unmask();
            },
            failure: function(result){
              clearTimeout(tid);
              el.unmask();
              failureCallback.call(this, result);
            }
          });
        } // canceled
      },
      scope: this
    });
  },
  
  toggleArchive: function(){
    var item = this.getSelectedRepository();
    if ( item ){
      item.archived = ! item.archived;
      if (debug){
        console.debug('toggle repository ' + item.name + ' archive to ' + item.archived);
      }
      
      var url = restUrl + 'repositories/' + item.id + '.json';
      this.executeRemoteCall(this.archiveTitleText, 
        String.format(this.archiveMsgText, item.name), 
        'PUT', url, item, function(result){
          main.handleFailure(
            result.status, 
            this.errorTitleText, 
            this.errorArchiveMsgText
          );
        }
      );
    }
  },

  removeRepository: function(){
    var item = this.getSelectedRepository();
    if ( item ){      
      if ( debug ){
        console.debug( 'remove repository ' + item.name );
      }

      var url = restUrl + 'repositories/' + item.id + '.json';
      this.executeRemoteCall(this.archiveTitleText, 
        String.format(this.archiveMsgText, item.name), 
        'DELETE', url, null, function(result){
          main.handleFailure(
            result.status, 
            this.errorTitleText, 
            this.errorMsgText
          );
        }
      );
    }
  },
  
  onRepositorySelection: function(item, owner){
    if ( owner ){
      if (state.clientConfig.enableRepositoryArchive){
        var archiveBt = Ext.getCmp('repoArchiveButton');
        if ( item.archived ){
          archiveBt.setText(this.unarchiveText);
          Ext.getCmp('repoRmButton').setDisabled(false);
        } else {
          archiveBt.setText(this.archiveText);
          Ext.getCmp('repoRmButton').setDisabled(true);
        }
        archiveBt.setDisabled(false);
      } else {
        Ext.getCmp('repoRmButton').setDisabled(false);
      }
    } else {
      Ext.getCmp('repoRmButton').setDisabled(false);
      if (state.clientConfig.enableRepositoryArchive){
        Ext.getCmp('repoArchiveButton').setDisabled(false);
      }
    }
  },

  resetPanel: function(){
    Sonia.repository.setEditPanel(Sonia.repository.DefaultPanel);
  },

  showAddForm: function(){
    Ext.getCmp('repoRmButton').setDisabled(true);
    Sonia.repository.setEditPanel([{
      xtype: 'repositorySettingsForm',
      listeners: {
        updated: {
          fn: this.reload,
          scope: this
        },
        created: {
          fn: this.repositoryCreated,
          scope: this
        }
      }
    }]);
  },
  
  repositoryCreated: function(item){
    var grid = this.getGrid();
    this.clearRepositoryFilter(grid);
    
    grid.reload(function(){
      if (debug){
        console.debug('select repository ' + item.id + " after creation");
      }
      grid.selectById(item.id);
    });
  },
  
  clearRepositoryFilter: function(grid){
    if (debug){
      console.debug('clear repository filter');
    }
    if (! grid ){
      grid = this.getGrid();
    }
    Ext.getCmp('repositorySearch').setValue('');
    Ext.getCmp('repositoryTypeFilter').setValue('');
    grid.clearStoreFilter();
  },

  reload: function(){
    this.getGrid().reload();
    var repo = this.getSelectedRepository();
    if ( repo ){
      this.onRepositorySelection(repo, Sonia.repository.isOwner(repo));
    }
  }

});

// register xtype
Ext.reg('repositoryPanel', Sonia.repository.Panel);

// register history handler
Sonia.History.register('repositoryPanel', {
  
  onActivate: function(panel){
    var token = null;
    var rec = panel.getGrid().getSelectionModel().getSelected();
    if (rec){
      token = Sonia.History.createToken('repositoryPanel', rec.get('id'));
    } else {
      token = Sonia.History.createToken('repositoryPanel');
    }
    return token;
  },
  
  waitAndSelect: function(grid, repoId){
    setTimeout(function(){
      if ( grid.ready ){
        grid.selectById(repoId);
      }
    }, 250);
  },
  
  onChange: function(repoId){
    var panel = Ext.getCmp('repositories');
    if ( ! panel ){
      main.addRepositoriesTabPanel();
      panel = Ext.getCmp('repositories');
      if ( repoId ){
        var selected = false;
        panel.getGrid().getStore().addListener('load', function(){
          if (!selected){
            panel.getGrid().selectById(repoId);
            selected = true;
          }
        });
      }
    } else {
      main.addTab(panel);
      if ( repoId ){
        var grid = panel.getGrid();
        if ( grid.ready ){
          grid.selectById(repoId);
        } else {
          this.waitAndSelect(grid, repoId);
        }
      }
    }
  }
});