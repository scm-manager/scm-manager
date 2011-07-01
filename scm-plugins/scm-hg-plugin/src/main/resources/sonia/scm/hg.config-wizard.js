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

Ext.ns("Sonia.hg");

Sonia.hg.ConfigWizard = Ext.extend(Ext.Window,{
  
  hgConfig: null,
  title: 'Mercurial Configuration Wizard',
  
  initComponent: function(){
    
    this.addEvents('finish');
    
    var config = {
      title: this.title,
      layout: 'fit',
      width: 420,
      height: 140,
      closable: true,
      resizable: true,
      plain: true,
      border: false,
      modal: true,
      bodyCssClass: 'x-panel-mc',
      items: [{
        id: 'hgConfigWizardPanel',
        xtype: 'hgConfigWizardPanel',
        hgConfig: this.hgConfig,
        listeners: {
          finish: {
            fn: this.onFinish,
            scope: this
          }
        }
      }]
    }
    
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.hg.ConfigWizard.superclass.initComponent.apply(this, arguments);
  },
  
  onFinish: function(config){
    this.fireEvent('finish', config);
    this.close();
  }
  
});

Sonia.hg.InstallationJsonReader = function(){
  this.RecordType = Ext.data.Record.create([{
    name: "path",
    mapping: "path",
    type: "string"
  }]);
};

Ext.extend(Sonia.hg.InstallationJsonReader, Ext.data.JsonReader, {
  
  readRecords: function(o){
    this.jsonData = o;
    
    if (debug){
      console.debug('read installation data from json');
      console.debug(o);
    }
    
    var records = [];
    var paths = o.path;
    for ( var i=0; i<paths.length; i++ ){
      records.push(new this.RecordType({
        'path': paths[i]
      }));
    }
    return {
      success: true,
      records: records,
      totalRecords: records.length
    };
  }
  
});

Sonia.hg.ConfigWizardPanel = Ext.extend(Ext.Panel,{
  
  hgConfig: null,
  packageTemplate: '<tpl for="."><div class="x-combo-list-item">\
                      {id} (hg: {hg-version}, py: {python-version}, size: {size:fileSize})\
                    </div></tpl>',
  
  // text
  backText: 'Back',
  nextText: 'Next',
  finishText: 'Finish',
  configureLocalText: 'Configure local installation',
  configureRemoteText: 'Download and install',
  loadingText: 'Loading ...',
  hgInstallationText: 'Mercurial Installation',
  pythonInstallationText: 'Python Installation',
  hgPackageText: 'Mercurial Package',
  errorTitleText: 'Error',
  packageInstallationFailedText: 'Package installation failed',
  installPackageText: 'install mercurial package {0}',
 
  initComponent: function(){
    this.addEvents('finish');
    
    var packageStore = new Ext.data.JsonStore({
      storeId: 'pkgStore',
      proxy: new Ext.data.HttpProxy({
        url: restUrl + 'config/repositories/hg/packages.json',
        disableCaching: false
      }),
      fields: [ 'id', 'hg-version', 'python-version', 'size' ],
      root: 'package',
      listeners: {
        load: {
          fn: this.checkIfPackageAvailable,
          scope: this
        }
      }
    });
    
    var hgInstallationStore = new Ext.data.Store({
      proxy: new  Ext.data.HttpProxy({
        url: restUrl + 'config/repositories/hg/installations/hg.json'
      }),
      fields: [ 'path' ],
      reader: new Sonia.hg.InstallationJsonReader(),
      autoLoad: true,
      autoDestroy: true
    });
    
    var pythonInstallationStore = new Ext.data.Store({
      proxy: new  Ext.data.HttpProxy({
        url: restUrl + 'config/repositories/hg/installations/python.json'
      }),
      fields: [ 'path' ],
      reader: new Sonia.hg.InstallationJsonReader(),
      autoLoad: true,
      autoDestroy: true
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
        scope: this
      },{
        id: 'finish',
        text: this.finishText,
        handler: this.applyChanges,
        scope: this,
        disabled: true
      }],
      items: [{
        id: 'cod',
        items: [{
          id: 'configureOrDownload',
          xtype: 'radiogroup',
          name: 'configureOrDownload',
          columns: 1,
          items: [{
            boxLabel: this.configureLocalText, 
            name: 'cod', 
            inputValue: 'localInstall',
            checked: true
          },{
            id: 'remoteInstallRadio',
            boxLabel: this.configureRemoteText, 
            name: 'cod', 
            inputValue: 'remoteInstall', 
            disabled: true
          }]
        }],
        listeners: {
          render: {
            fn: function(panel){
              panel.body.mask(this.loadingText);
              var store = Ext.StoreMgr.lookup('pkgStore');
              store.load.defer(100, store);
            },
            scope: this
          }
        }
      },{
        id: 'localInstall',
        layout: 'form',
        defaults: {
          width: 250
        },
        items: [{
          id: 'mercurial',
          fieldLabel: this.hgInstallationText,
          name: 'mercurial',
          xtype: 'combo',
          readOnly: false,
          triggerAction: 'all',
          lazyRender: true,
          mode: 'local',
          editable: true,
          store: hgInstallationStore,
          valueField: 'path',
          displayField: 'path',
          allowBlank: false,
          value: this.hgConfig.hgBinary
        },{
          id: 'python',
          fieldLabel: this.pythonInstallationText,
          name: 'python',
          xtype: 'combo',
          readOnly: false,
          triggerAction: 'all',
          lazyRender: true,
          mode: 'local',
          editable: true,
          store: pythonInstallationStore,
          valueField: 'path',
          displayField: 'path',
          allowBlank: false,
          value: this.hgConfig.pythonBinary
        }]
      },{
        id: 'remoteInstall',
        layout: 'form',
        defaults: {
          width: 250
        },
        items: [{
          id: 'package',
          fieldLabel: this.hgPackageText,
          name: 'package',
          xtype: 'combo',
          readOnly: false,
          triggerAction: 'all',
          lazyRender: true,
          mode: 'local',
          editable: false,
          store: packageStore,
          valueField: 'id',
          displayField: 'id',
          allowBlank: false,
          tpl: this.packageTemplate,
          listeners: {
            select: function(){
              Ext.getCmp('finish').setDisabled(false);
            }
          }
        }]
      }]
    }
    
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.hg.ConfigWizardPanel.superclass.initComponent.apply(this, arguments);
  },
  
  checkIfPackageAvailable: function(store){
    Ext.getCmp('cod').body.unmask();
    var c = store.getTotalCount();
    if ( debug ){
      console.debug( "found " + c + " package(s)" );
    }
    if ( c > 0 ){
      Ext.getCmp('remoteInstallRadio').setDisabled(false);
    }
  },
  
  navHandler: function(direction){
    var layout = this.getLayout();
    var id = layout.activeItem.id;
    
    var next = -1;
    
    if ( id == 'cod' && direction == 1 ){
      var v = Ext.getCmp('configureOrDownload').getValue().getRawValue();
      var df = false;
      if ( v == 'localInstall' ){
        next = 1;
      } else if ( v == 'remoteInstall' ){
        next = 2;
        df = true;
      }
      Ext.getCmp('move-prev').setDisabled(false);
      Ext.getCmp('move-next').setDisabled(true);
      Ext.getCmp('finish').setDisabled(df);
    } 
    else if (direction == -1 && (id == 'localInstall' || id == 'remoteInstall')) {
      next = 0;
      Ext.getCmp('move-prev').setDisabled(true);
      Ext.getCmp('move-next').setDisabled(false);
      Ext.getCmp('finish').setDisabled(true);   
    }
    
    if ( next >= 0 ){
      layout.setActiveItem(next);
    }
  },
  
  applyChanges: function(){
    var v = Ext.getCmp('configureOrDownload').getValue().getRawValue();
    if ( v == 'localInstall' ){
      this.applyLocalConfiguration();
    } else if ( v == 'remoteInstall' ){
      this.applyRemoteConfiguration();
    }
  },
  
  applyRemoteConfiguration: function(){
    if ( debug ){
      console.debug( "apply remote configuration" );
    }
    
    var pkg = Ext.getCmp('package').getValue();
    if ( debug ){
      console.debug( 'install mercurial package ' + pkg );
    }
    
    var lbox = Ext.MessageBox.show({
      title: this.loadingText,
      msg: String.format(this.installPackageText, pkg),
      width: 300,
      wait: true,
      animate: true,
      progress: true,
      closable: false
    });
    
    Ext.Ajax.request({
      url: restUrl + 'config/repositories/hg/packages/' + pkg + '.json',
      method: 'POST',
      scope: this,
      timeout: 900000, // 15min
      success: function(){
        if ( debug ){
          console.debug('package successfully installed');
        }
        lbox.hide();
        this.fireEvent('finish');
      },
      failure: function(){
        if ( debug ){
          console.debug('package installation failed');
        }
        lbox.hide();
        Ext.MessageBox.show({
          title: this.errorTitleText,
          msg: this.packageInstallationFailedText,
          buttons: Ext.MessageBox.OK,
          icon:Ext.MessageBox.ERROR
        });
      }
    });
    
    
  },
  
  applyLocalConfiguration: function(){
    if ( debug ){
      console.debug( "apply remote configuration" );
    }
    var mercurial = Ext.getCmp('mercurial').getValue();
    var python = Ext.getCmp('python').getValue();
    if (debug){
      console.debug( 'configure mercurial=' + mercurial + " and python=" + python );
    }
    delete this.hgConfig.pythonPath;
    delete this.hgConfig.useOptimizedBytecode;
    this.hgConfig.hgBinary = mercurial;
    this.hgConfig.pythonBinary = python;
    
    if ( debug ){
      console.debug( this.hgConfig );
    }
    
    this.fireEvent('finish', this.hgConfig);
  }
  
});

// register xtype
Ext.reg('hgConfigWizardPanel', Sonia.hg.ConfigWizardPanel);


// i18n

if ( i18n != null && i18n.country == 'de' ){

  Ext.override(Sonia.hg.ConfigWizardPanel, {

    backText: 'Zurück',
    nextText: 'Weiter',
    finishText: 'Fertigstellen',
    configureLocalText: 'Eine lokale Installation Konfigurieren',
    configureRemoteText: 'Herunterladen und installieren',
    loadingText: 'Lade ...',
    hgInstallationText: 'Mercurial Installation',
    pythonInstallationText: 'Python Installation',
    hgPackageText: 'Mercurial Package',
    errorTitleText: 'Fehler',
    packageInstallationFailedText: 'Package Installation fehlgeschlagen',
    installPackageText: 'Installiere Mercurial-Package {0}'

  });

}