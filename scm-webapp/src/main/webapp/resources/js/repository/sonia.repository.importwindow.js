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
  closeBtnText: 'Close',
  
  imported: [],
  failed: [],
  
  // help text
  importTypeDirectoryHelpText: 'Imports all repositories that are located at the repository folder of SCM-Manager.',
  importTypeURLHelpText: 'Imports a repository from a remote url.',
  importTypeFileHelpText: 'Imports a repository from a file (e.g. SVN dump).',
  
  importUrlNameHelpText: 'The name of the repository in SCM-Manager.',
  importUrlHelpText: 'The source url of the repository.',
  
  importFileNameHelpText: 'The name of the repository in SCM-Manager.',
  importFileHelpText: 'Choose the dump file you want to import to SCM-Manager.',
  importFileGZipCompressedHelpText: 'The file is gzip compressed.',
  
  // tips
  tipRepositoryType: 'Choose your repository type for the import.',
  tipImportType: 'Select the type of import. <b>Note:</b> Not all repository types support all options.',
  
  // cache
  nextButton: null,
  prevButton: null,
  
  // settings
  repositoryType: null,
  
  // active card
  activeForm: null,

  // result template
  tpl: new Ext.XTemplate([
    '<p style="padding: 0 10px">',
    '  <tpl if="imported">',
    '    <b>Imported repositories</b><br />',
    '    <tpl for="imported">',
    '      - {.}<br />',
    '    </tpl>',
    '    <br />',
    '  </tpl>',
    '  <tpl if="failed">',
    '    <b>Failed to import the following directories</b><br />',
    '    <tpl for="failed">',
    '      - {.}<br />',
    '    </tpl>',
    '  </tpl>',
    '  <tpl if="isEmpty(imported, failed)">',
    '    <b>No repositories to import</b>',
    '  </tpl>',
    '</p>',
    '<br />'
  ]),
  
  initComponent: function(){
    this.addEvents('finish');

    // fix initialization bug
    this.imported = [];
    this.failed = [];
    this.activeForm = null;
    
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
        id: 'closeBtn',
        text: this.closeBtnText,
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
            change: {
              fn: function(){
                this.getNextButton().setDisabled(false);
              },
              scope: this
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
            change: {
              fn: function(){
                this.getNextButton().setDisabled(false);
              },
              scope: this
            }
          }
        }]
      },{
        id: 'importUrlLayout',
        xtype: 'form',
        monitorValid: true,
        defaults: {
          width: 250
        },
        listeners: {
          clientvalidation: {
            fn: this.urlFormValidityMonitor,
            scope: this
          }
        },
        items: [{
          id: 'importUrlName',
          xtype: 'textfield',
          fieldLabel: 'Repository name',
          name: 'name',
          allowBlank: false,
          vtype: 'repositoryName',
          helpText: this.importUrlNameHelpText
        },{
          id: 'importUrl',
          xtype: 'textfield',
          fieldLabel: 'Import URL',
          name: 'url', 
          allowBlank: false,
          vtype: 'url',
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
        monitorValid: true,
        listeners: {
          clientvalidation: {
            fn: this.fileFormValidityMonitor,
            scope: this
          }
        },
        items: [{
          id: 'importFileName',
          xtype: 'textfield',
          fieldLabel: 'Repository name',
          name: 'name', 
          type: 'textfield',
          width: 250,
          allowBlank: false,
          vtype: 'repositoryName',
          helpText: this.importFileNameHelpText
        },{
          id: 'importFile',
          xtype: 'fileuploadfield',
          fieldLabel: 'Import File',
          ctCls: 'import-fu',
          name: 'bundle', 
          allowBlank: false,
          helpText: this.importFileHelpText,
          cls: 'import-fu',
          buttonCfg: {
            iconCls: 'upload-icon'
          }
        },{
          id: 'importFileGZipCompressed',
          xtype: 'checkbox',
          fieldLabel: 'GZip compressed',
          helpText: this.importFileGZipCompressedHelpText
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
          id: 'resultPanel',
          xtype: 'panel',
          bodyCssClass: 'x-panel-mc',
          tpl: this.tpl
        },{
          id: 'finalTip',
          xtype: 'scmTip',
          content: 'Please see log for details.',
          width: '100%',
          hidden: true
        }]
      }]
    };
    
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.repository.ImportPanel.superclass.initComponent.apply(this, arguments);
  },
  
  navHandler: function(direction){
    this.activeForm = null;
    
    var layout = this.getLayout();
    var id = layout.activeItem.id;
    
    var next = -1;
    
    if ( id === 'repositoryTypeLayout' && direction === 1 ){
      this.repositoryType = Ext.getCmp('chooseRepositoryType').getValue().getRawValue();
      this.enableAvailableImportTypes();
      next = 1;
    } 
    else if ( id === 'importTypeLayout' && direction === -1 ){
      next = 0;
      Ext.getCmp('move-prev').setDisabled(true);
      Ext.getCmp('move-next').setDisabled(false);
    }
    else if ( id === 'importTypeLayout' && direction === 1 ){
      Ext.getCmp('move-next').setDisabled(false);
      var v = Ext.getCmp('chooseImportType').getValue();
      if ( v ){
        switch (v.getRawValue()){
          case 'directory':
            this.importFromDirectory(layout);
            break;
          case 'url':
            next = 2;
            this.activeForm = 'url';
            break;
          case 'file':
            next = 3;
            this.activeForm = 'file';
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
  
  getNextButton: function(){
    if (!this.nextButton){
      this.nextButton = Ext.getCmp('move-next');
    }
    return this.nextButton;
  },
  
  getPrevButton: function(){
    if (!this.prevButton){
      this.prevButton = Ext.getCmp('move-prev');
    }
    return this.prevButton;
  },
    
  showLoadingBox: function(){
    return Ext.MessageBox.show({
      title: 'Loading',
      msg: 'Import repository',
      width: 300,
      wait: true,
      animate: true,
      progress: true,
      closable: false
    });
  },
  
  urlFormValidityMonitor: function(form, valid){
    if (this.activeForm === 'url'){
      this.formValidityMonitor(valid);
    }
  },
  
  fileFormValidityMonitor: function(form, valid){
    if (this.activeForm === 'file'){
      this.formValidityMonitor(valid);
    }
  },
  
  formValidityMonitor: function(valid){
    var nbt = this.getNextButton();
    if (valid && nbt.disabled){
      nbt.setDisabled(false);
    } else if (!valid && !nbt.disabled){
      nbt.setDisabled(true);
    }
  },
  
  appendImportResult: function(layout, result){
    for (var i=0; i<result.importedDirectories.length; i++){
      this.imported.push(result.importedDirectories[i]);
    }
    for (var i=0; i<result.failedDirectories.length; i++){
      this.failed.push(result.failedDirectories[i]);
    }
    this.displayResult(layout);
  },
  
  appendImported: function(layout, name){
    this.imported.push(name);
    this.displayResult(layout);
  },
  
  appendFailed: function(layout, name){
    this.failed.push(name);
    this.displayResult(layout);
    layout.setActiveItem(4);
  },
  
  displayResult: function(layout){
    this.getNextButton().setDisabled(true);
    this.getPrevButton().setDisabled(true);
    Ext.getCmp('closeBtn').setDisabled(false);
    
    var model = {
      imported: this.imported.length > 0 ? this.imported : null, 
      failed: this.failed.length > 0 ? this.failed : null,
      isEmpty: function(imported, failed){
        return !imported && !failed;
      }
    };
    var resultPanel = Ext.getCmp('resultPanel');
    Ext.getCmp('finalTip').setVisible(this.failed.length > 0);
    resultPanel.tpl.overwrite(resultPanel.body, model);
    layout.setActiveItem(4);
  },
  
  importFromFile: function(layout, form){
    var compressed = Ext.getCmp('importFileGZipCompressed').getValue();
    var lbox = this.showLoadingBox();
    form.submit({
      url: restUrl + 'import/repositories/' + this.repositoryType + '/bundle.html?compressed=' + compressed,
      timeout: 300000, // 5min
      scope: this,
      success: function(form){
        lbox.hide();
        this.appendImported(layout, form.getValues().name);
      },
      failure: function(form){
        lbox.hide();
        this.appendFailed(layout, form.getValues().name);
      }
    });
  },
  
  importFromUrl: function(layout, repository){
    var lbox = this.showLoadingBox();
    Ext.Ajax.request({
      url: restUrl + 'import/repositories/' + this.repositoryType + '/url.json',
      method: 'POST',
      scope: this,
      timeout: 300000, // 5min
      jsonData: repository,
      success: function(){
        lbox.hide();
        this.appendImported(layout, repository.name);
      },
      failure: function(){
        lbox.hide();
        this.appendFailed(layout, repository.name);
      }
    });    
  },
  
  importFromDirectory: function(layout){
    var lbox = this.showLoadingBox();
    Ext.Ajax.request({
      url: restUrl + 'import/repositories/' + this.repositoryType + '/directory.json',
      method: 'POST',
      scope: this,
      success: function(response){
        lbox.hide();
        this.appendImportResult(layout, Ext.decode(response.responseText));
      },
      failure: function(result){
        lbox.hide();
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
      this.getNextButton().setDisabled(true);
      this.getPrevButton().setDisabled(false);
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