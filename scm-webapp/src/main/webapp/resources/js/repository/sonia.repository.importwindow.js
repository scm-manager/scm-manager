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

Sonia.repository.ImportWindow =  Ext.extend(Ext.Window,{
  
  title: 'Repository Import Wizard',
  
  initComponent: function(){
    
    this.addEvents('finish');
    
    var config = {
      title: this.title,
      layout: 'fit',
      width: 420,
      height: 190,
      closable: true,
      resizable: true,
      plain: true,
      border: false,
      modal: true,
      bodyCssClass: 'x-panel-mc',
      items: [{
        id: 'scmRepositoryImportWizard',
        xtype: 'scmRepositoryImportWizard',
        listeners: {
          finish: {
            fn: this.onFinish,
            scope: this
          }
        }
      }]
    };
    
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.repository.ImportWindow.superclass.initComponent.apply(this, arguments);
  },
  
  onFinish: function(config){
    this.fireEvent('finish', config);
    this.close();
  }
  
});

Sonia.repository.ImportPanel = Ext.extend(Ext.Panel, {
  
    // text
  backText: 'Back',
  nextText: 'Next',
  finishText: 'Finish',
  
  imported: [],
  importJobsFinished: 0,
  importJobs: 0,
  
  // help text
  importTypeDirectoryHelpText: 'Imports all repositories that are located at the repository folder of SCM-Manager.',
  importTypeURLHelpText: 'Imports a repository from a remote url.',
  importTypeFileHelpText: 'Imports a repository from a file (e.g. SVN dump).',
  
  importUrlNameHelpText: 'The name of the repository in SCM-Manager.',
  importUrlHelpText: 'The source url of the repository.',
  
  importFileNameHelpText: 'The name of the repository in SCM-Manager.',
  importFileHelpText: 'Choose the dump file you want to import to SCM-Manager.',
  
  // tips
  tipRepositoryType: 'Choose your repository type for the import.',
  tipImportType: 'Select the type of import. <b>Note:</b> Not all repository types support all options.',
  
  // settings
  repositoryType: null,
  
  initComponent: function(){
    this.addEvents('finish');

    // fix initialization bug
    this.imported = [];
    this.importJobsFinished = 0;
    this.importJobs = 0;
    
    var importedStore = new Ext.data.JsonStore({
      fields: ['type', 'name']
    });
    // store.loadData(this.imported);
    
    var importedColModel = new Ext.grid.ColumnModel({
      defaults: {
        sortable: true,
        scope: this
      },
      columns: [
        {id: 'name', header: 'Name', dataIndex: 'name'},
        {id: 'type', header: 'Type', dataIndex: 'type'}
      ]
    });
    
    var typeItems = [];
  
    Ext.each(state.repositoryTypes, function(repositoryType){
      typeItems.push({
        boxLabel: repositoryType.displayName,
        name: 'repositoryType', 
        inputValue: repositoryType.name,
        checked: false
      });
    });
    
    typeItems = typeItems.sort(function(a, b){
      return a.boxLabel > b.boxLabel;
    });
    
    typeItems.push({
      xtype: 'scmTip',
      content: this.tipRepositoryType,
      width: '100%'
    });
    
    var config = {
      layout: 'card',
      activeItem: 0,
      bodyStyle: 'padding: 5px',
      defaults: {
        bodyCssClass: 'x-panel-mc',
        border: false,
        labelWidth: 120,
        width: 250
      },
      bbar: ['->',{
        id: 'move-prev',
        text: this.backText,
        handler: this.navHandler.createDelegate(this, [-1]),
        disabled: true,
        scope: this
      },{
        id: 'move-next',
        text: this.nextText,
        handler: this.navHandler.createDelegate(this, [1]),
        disabled: true,
        scope: this
      },{
        id: 'finish',
        text: this.finishText,
        handler: this.applyChanges,
        disabled: true,
        scope: this
      }],
      items: [{
        id: 'repositoryTypeLayout',
        items: [{
          id: 'chooseRepositoryType',
          xtype: 'radiogroup',
          name: 'chooseRepositoryType',
          columns: 1,
          items: [typeItems],
          listeners: {
            change: function(){
              Ext.getCmp('move-next').setDisabled(false);
            }
          }
        }]
      },{
        id: 'importTypeLayout',
        items: [{
          id: 'chooseImportType',
          xtype: 'radiogroup',
          name: 'chooseImportType',
          columns: 1,
          items: [{
            id: 'importTypeDirectory',
            boxLabel: 'Import from directory',
            name: 'importType', 
            inputValue: 'directory',
            disabled: false,
            helpText: this.importTypeDirectoryHelpText
          },{
            id: 'importTypeURL',
            boxLabel: 'Import from url',
            name: 'importType', 
            inputValue: 'url',
            checked: false,
            disabled: true,
            helpText: this.importTypeURLHelpText
          },{
            id: 'importTypeFile',
            boxLabel: 'Import from file',
            name: 'importType', 
            inputValue: 'file',
            checked: false,
            disabled: true,
            helpText: this.importTypeFileHelpText
          },{
            xtype: 'scmTip',
            content: this.tipImportType,
            width: '100%'
          }],
          listeners: {
            change: function(){
              Ext.getCmp('move-next').setDisabled(false);
            }
          }
        }]
      },{
        id: 'importUrlLayout',
        xtype: 'form',
        defaults: {
          width: 250
        },
        items: [{
          id: 'importUrlName',
          xtype: 'textfield',
          fieldLabel: 'Repository name',
          name: 'name', 
          type: 'textfield',
          disabled: false,
          helpText: this.importUrlNameHelpText
        },{
          id: 'importUrl',
          xtype: 'textfield',
          fieldLabel: 'Import URL',
          name: 'url', 
          disabled: false,
          helpText: this.importUrlHelpText
        },{
          xtype: 'scmTip',
          content: 'Please insert name and remote url of the repository.',
          width: '100%'
        }]
      },{
        id: 'importFileLayout',
        xtype: 'form',
        fileUpload: true,
        items: [{
          id: 'importFileName',
          xtype: 'textfield',
          fieldLabel: 'Repository name',
          name: 'name', 
          type: 'textfield',
          width: 250,
          helpText: this.importFileNameHelpText
        },{
          id: 'importFile',
          xtype: 'fileuploadfield',
          fieldLabel: 'Import File',
          ctCls: 'import-fu',
          name: 'bundle', 
          helpText: this.importFileHelpText,
          cls: 'import-fu',
          buttonCfg: {
            iconCls: 'upload-icon'
          }
        },{
          xtype: 'scmTip',
          content: 'Please insert name and upload the repository file.',
          width: '100%'
        }]
      },{
        id: 'importFinishedLayout',
        layout: 'form',
        defaults: {
          width: 250
        },
        items: [{
          id: 'importedGrid',
          xtype: 'grid',
          autoExpandColumn: 'name',
          store: importedStore,
          colModel: importedColModel,
          height: 100
        }]
      }]
    };
    
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.repository.ImportPanel.superclass.initComponent.apply(this, arguments);
  },
  
  navHandler: function(direction){
    var layout = this.getLayout();
    var id = layout.activeItem.id;
    
    var next = -1;
    
    if ( id === 'repositoryTypeLayout' && direction === 1 ){
      this.repositoryType = Ext.getCmp('chooseRepositoryType').getValue().getRawValue();
      console.log('rt: ' + this.repositoryType);
      this.enableAvailableImportTypes();
      next = 1;
    } 
    else if ( id === 'importTypeLayout' && direction === -1 ){
      next = 0;
      Ext.getCmp('move-prev').setDisabled(true);
      Ext.getCmp('move-next').setDisabled(false);
    }
    else if ( id === 'importTypeLayout' && direction === 1 ){
      var v = Ext.getCmp('chooseImportType').getValue();
      if ( v ){
        switch (v.getRawValue()){
          case 'directory':
            this.importFromDirectory(layout);
            break;
          case 'url':
            next = 2;
            break;
          case 'file':
            next = 3;
            break;
        }
      }
    }
    else if ( (id === 'importUrlLayout' || id === 'importFileLayout') && direction === -1 )
    {
      next = 1;
    }
    else if ( id === 'importUrlLayout' && direction === 1 )
    {
      this.importFromUrl(layout, Ext.getCmp('importUrlLayout').getForm().getValues());
    }
    else if ( id === 'importFileLayout' && direction === 1 )
    {
      this.importFromFile(layout, Ext.getCmp('importFileLayout').getForm());
    }
    
    if ( next >= 0 ){
      layout.setActiveItem(next);
    }
  },
  
  appendImported: function(repositories){
    for (var i=0; i<repositories.length; i++){
      this.imported.push(repositories[i]);
    }
    this.importJobsFinished++;
    if ( this.importJobsFinished >= this.importJobs ){
      if (debug){
        console.debug( 'import of ' + this.importJobsFinished + ' jobs finished'  );
      }
      Ext.getCmp('importedGrid').getStore().loadData(this.imported);
      Ext.getCmp('move-next').setDisabled(true);
      Ext.getCmp('move-prev').setDisabled(true);
      Ext.getCmp('finish').setDisabled(false);
    }
  },
  
  importFromFile: function(layout, form){
    form.submit({
      url: restUrl + 'import/repositories/' + this.repositoryType + '/bundle.html',
      scope: this,
      success: function(form, action){
        this.appendImported([{
          name: form.getValues().name,
          type: this.repositoryType
        }]);
        layout.setActiveItem(4);
      },
      failure: function(form, action){
        main.handleRestFailure(
          action.response, 
          this.errorTitleText, 
          this.errorMsgText
        );
      }
    });
  },
  
  importFromUrl: function(layout, repository){
    Ext.Ajax.request({
      url: restUrl + 'import/repositories/' + this.repositoryType + '/url.json',
      method: 'POST',
      scope: this,
      jsonData: repository,
      success: function(){
        this.appendImported([{
          name: repository.name,
          type: this.repositoryType
        }]);
        layout.setActiveItem(4);
      },
      failure: function(result){
        main.handleRestFailure(
          result, 
          this.errorTitleText, 
          this.errorMsgText
        );
      }
    });    
  },
  
  importFromDirectory: function(layout){
    Ext.Ajax.request({
      url: restUrl + 'import/repositories/' + this.repositoryType + '.json',
      method: 'POST',
      scope: this,
      success: function(response){
        var obj = Ext.decode(response.responseText);
        this.appendImported(obj);
        layout.setActiveItem(4);
      },
      failure: function(result){
        main.handleRestFailure(
          result, 
          this.errorTitleText, 
          this.errorMsgText
        );
      }
    });
  },
  
  enableAvailableImportTypes: function(){
    var type = null;
    Ext.each(state.repositoryTypes, function(repositoryType){
      if (repositoryType.name === this.repositoryType){
        type = repositoryType;
      }
    }, this);
    
    if ( type !== null ){
      Ext.getCmp('chooseImportType').setValue(null);
      Ext.getCmp('move-next').setDisabled(true);
      Ext.getCmp('move-prev').setDisabled(false);
      Ext.getCmp('importTypeURL').setDisabled(type.supportedCommands.indexOf('PULL') < 0);
      Ext.getCmp('importTypeFile').setDisabled(type.supportedCommands.indexOf('UNBUNDLE') < 0);
    }
  },
  
  applyChanges: function(){
    var panel = Ext.getCmp('repositories');
    if (panel){
      panel.getGrid().reload();
    }
    this.fireEvent('finish');
  }
  
});

// register xtype
Ext.reg('scmRepositoryImportWizard', Sonia.repository.ImportPanel);