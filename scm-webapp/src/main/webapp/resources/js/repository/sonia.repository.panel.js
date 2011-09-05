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


// RepositoryPanel
Sonia.repository.Panel = Ext.extend(Sonia.rest.Panel, {

  titleText: 'Repository Form',
  emptyText: 'Add or select an Repository',
  removeTitleText: 'Remove Repository',
  removeMsgText: 'Remove Repository "{0}"?',
  errorTitleText: 'Error',
  errorMsgText: 'Repository deletion failed',

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
      text: 'Filter: '
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
      text: 'Search: '
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

    var config = {
      tbar: toolbar,
      items: [{
          id: 'repositoryGrid',
          xtype: 'repositoryGrid',
          region: 'center'
        }, {
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
        }
      ]
    }

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.repository.Panel.superclass.initComponent.apply(this, arguments);
  },
  
  filterByType: function(combo, rec){
    Ext.getCmp('repositoryGrid').filter(rec.get('name'));
  },
  
  search: function(field){
    Ext.getCmp('repositoryGrid').search(field.getValue());
  },

  removeRepository: function(){
    var grid = Ext.getCmp('repositoryGrid');
    var selected = grid.getSelectionModel().getSelected();
    if ( selected ){
      var item = selected.data;
      var url = restUrl + 'repositories/' + item.id + '.json';
      
      Ext.MessageBox.show({
        title: this.removeTitleText,
        msg: String.format(this.removeMsgText, item.name),
        buttons: Ext.MessageBox.OKCANCEL,
        icon: Ext.MessageBox.QUESTION,
        fn: function(result){
          if ( result == 'ok' ){

            if ( debug ){
              console.debug( 'remove repository ' + item.name );
            }

            Ext.Ajax.request({
              url: url,
              method: 'DELETE',
              scope: this,
              success: function(){
                this.reload();
                this.resetPanel();
              },
              failure: function(result){
                main.handleFailure(
                  result.status, 
                  this.errorTitleText, 
                  this.errorMsgText
                );
              }
            });
          }

        },
        scope: this
      });

    } else if ( debug ){
      console.debug( 'no repository selected' );
    }
  },

  resetPanel: function(){
    Sonia.repository.setEditPanel(Sonia.repository.DefaultPanel);
  },

  showAddForm: function(){
    Ext.getCmp('repoRmButton').setDisabled(true);
    Sonia.repository.setEditPanel([{
      xtype: 'repositoryPropertiesForm',
      listeners: {
        updated: {
          fn: this.reload,
          scope: this
        },
        created: {
          fn: this.clearRepositoryFilter,
          scope: this
        }
      }
    }]);
  },
  
  clearRepositoryFilter: function(){
    if (debug){
      console.debug('clear repository filter');
    }
    Ext.getCmp('repositorySearch').setValue('');
    Ext.getCmp('repositoryTypeFilter').setValue('');
    var grid = Ext.getCmp('repositoryGrid');
    grid.clearStoreFilter();
    grid.reload();

  },

  reload: function(){
    Ext.getCmp('repositoryGrid').reload();
  }

});

// register xtype
Ext.reg('repositoryPanel', Sonia.repository.Panel);