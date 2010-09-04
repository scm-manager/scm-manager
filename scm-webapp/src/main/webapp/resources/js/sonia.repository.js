/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

Ext.ns('Sonia.repository');

Sonia.repository.Grid = Ext.extend(Ext.grid.GridPanel, {

  initComponent: function(){

    var repositoryStore = new Ext.data.JsonStore({
      url: 'rest/repositories.json',
      root: 'repository',
      fields: [ 'name', 'type', 'contact', 'description' ],
      sortInfo: {
        field: 'name'
      },
      autoLoad: true,
      listeners: {
        // fix jersey empty array problem
        exception: {
          fn: function(proxy, type, action, options, response, arg){
            if ( action == "read" && response.responseText == "null" ){
              this.store.removeAll();
            }
          },
          scope: this
        }
      }
    });

    var repositoryColModel = new Ext.grid.ColumnModel({
      columns: [
        {header: 'Name', sortable: true, width: 100, dataIndex: 'name'},
        {header: 'Type', sortable: true, width: 50, dataIndex: 'type'},
        {header: 'Contact', sortable: true, width: 100, dataIndex: 'contact'},
        {header: 'Description', sortable: true, dataIndex: 'description'}
      ]
    });

    var repositorySelModel = new Ext.grid.RowSelectionModel({
      singleSelect: true
    });

    var repositoryToolbar = new Ext.Toolbar({
      items: [
        {xtype: 'tbbutton', text: 'Add', scope: this, handler: this.showAddWindow},
        {xtype: 'tbbutton', text: 'Edit', scope: this, handler: this.showEditWindow},
        {xtype: 'tbbutton', text: 'Remove', scope: this, handler: this.remove},
        {xtype: 'tbbutton', text: 'Reload', scope: this, handler: this.reload},
        {xtype: 'tbseparator'},
        {xtype: 'label', text: 'Search: '},
        {xtype: 'textfield', listeners: {
          specialkey: {
            fn: function(field, e){
              if (e.getKey() == e.ENTER) {
                this.search(field.getValue());
              }
            },
            scope: this
          }
        }}
      ]
    });

    var config = {
      store: repositoryStore,
      colModel: repositoryColModel,
      selModel: repositorySelModel,
      tbar: repositoryToolbar,
      viewConfig: {
        forceFit: true
      },
      loadMask: true,
      listeners: {
        celldblclick: this.showEditWindow
      }
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.repository.Grid.superclass.initComponent.apply(this, arguments);
  },

  search: function( value ){
    this.store.filter('name', new RegExp('.*' + value + '.*'));
  },

  showAddWindow: function(){
    this.showFormWindow(new Sonia.repository.EditForm());
  },

  showEditWindow: function(){
    if ( this.selModel.hasSelection() ){
      var repository = this.selModel.getSelected().data;
      var form = new Sonia.repository.EditForm({
        url: 'rest/repositories/' + repository.name + ".json",
        method: 'PUT',
        update: true
      });
      form.load( repository );
      this.showFormWindow(form);
    }
  },

  showFormWindow: function( form ){
    var win = new Sonia.repository.DetailWindow();
    form.on('finish', function(reload){
      win.close();
      if ( reload ){
        this.store.reload();
      }
    }, this);
    win.add(form);
    win.show();
  },

  remove: function(){
    if ( this.selModel.hasSelection() ){
      var repository = this.selModel.getSelected().data.name;

      var grid = this;

      Ext.MessageBox.show({
        title: "Remove Repository?",
        msg: "Remove Repository '" + repository + "'?",
        buttons: Ext.MessageBox.OKCANCEL,
        icon: Ext.MessageBox.QUESTION,
        scope: this,
        fn: function(result){
          if ( result == "ok" ){
            Ext.Ajax.request({
              url: 'rest/repositories/' + repository + ".json",
              method: 'DELETE',
              success: function(){
                grid.reload();
              },
              failure: function(){
                alert("ERROR!!!")
              }
            });
          }
        }
      });
    }
  },

  reload: function(){
    this.store.reload();
  }

});

Ext.reg('repositoryGrid', Sonia.repository.Grid);

Sonia.repository.EditForm = Ext.extend(Ext.form.FormPanel, {

  url: 'rest/repositories.json',
  method: 'POST',
  update: false,

  initComponent: function(){

    var config = {
      labelWidth: 80,
      autoHeight: true,
      frame: true,
      url: this.url,
      title: 'Edit Repository',
      defaultType:'textfield',
      monitorValid: true,
      defaults : { width: 190 },
      items:[
        {fieldLabel: 'Name', name: 'name', readOnly: this.update, allowBlank: false},

        // TODO: replace store with dynamic one
        {
         fieldLabel: 'Type',
         name: 'type',
         xtype: 'combo',
         hiddenName : 'type',
         typeAhead: true,
         triggerAction: 'all',
         lazyRender: true,
         readOnly: this.update,
         mode: 'local',
         editable: false,
         store: repositoryTypeStore,
         valueField: 'type',
         displayField: 'name',
         allowBlank: false
        },
       
        {fieldLabel: 'Contact', name: 'contact'},
        {fieldLabel: 'Description', name: 'description', xtype: 'textarea'}
      ],
      buttons:[
        {text: 'Ok', formBind: true, scope: this, handler: this.submit},
        {text: 'Cancel', scope: this, handler: this.cancel}
      ]
    };

    this.addEvents('finish');

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.repository.EditForm.superclass.initComponent.apply(this, arguments);
  },

  load: function(repository){
    var data = { success: true, data: repository };
    this.getForm().loadRecord( data );
  },

  submit: function(){
    var editForm = this;

    this.getForm().submit({
      method: editForm.method,
      success: function(){
        editForm.fireEvent('finish', true);
      },
      failure: function(){
        alert( "failure!!!" );
      }
    });
  },

  cancel: function(){
    this.fireEvent('finish', false);
  }

});

Ext.reg('repositoryEditForm', Sonia.repository.EditForm);


Sonia.repository.DetailWindow = Ext.extend(Ext.Window, {

  initComponent: function(){
    var config = {
      layout:'fit',
      width:300,
      autoScroll: true,
      closable: false,
      resizable: false,
      plain: true,
      border: false,
      modal: true
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.repository.DetailWindow.superclass.initComponent.apply(this, arguments);
  }
  
});

Ext.reg('repositoryDetailWindow', Sonia.repository.DetailWindow);
