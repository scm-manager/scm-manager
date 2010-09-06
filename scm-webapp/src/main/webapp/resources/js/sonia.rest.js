/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

Ext.ns("Sonia.rest");

Sonia.rest.JsonStore = Ext.extend( Ext.data.JsonStore, {

  constructor: function(config) {
    var baseConfig = {
      autoLoad: false,
      listeners: {
        // fix jersey empty array problem
        exception: {
          fn: function(proxy, type, action, options, response, arg){
            if ( action == 'read' && response.responseText == 'null' ){
              if ( debug ){
                console.debug( 'empty array, clear whole store' );
              }
              this.removeAll();
            }
          },
          scope: this
        }
      }
    };
    Sonia.rest.JsonStore.superclass.constructor.call(this, Ext.apply(config, baseConfig));
  }

});

Sonia.rest.EditForm = Ext.extend(Ext.form.FormPanel, {

  title: 'Edit REST',
  data: null,
  
  initComponent: function(){

    var config = {
      labelWidth: 80,
      autoHeight: true,
      frame: true,
      title: this.title,
      defaultType:'textfield',
      monitorValid: true,
      defaults: {width: 190},
      buttons:[
        {text: 'Ok', formBind: true, scope: this, handler: this.submit},
        {text: 'Cancel', scope: this, handler: this.cancel}
      ]
    };

    this.addEvents('submit', 'cancel');

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.rest.EditForm.superclass.initComponent.apply(this, arguments);

    if ( this.data != null ){
      this.load(this.data);
    }
  },

  load: function(item){
    var data = {success: true, data: item};
    this.getForm().loadRecord( data );
  },

  submit: function(){
    var form = this.getForm();
    var item = this.getItem( form );
    this.fireEvent('submit', item);
  },

  getItem: function(form){
    return form.getFieldValues();
  },

  cancel: function(){
    this.fireEvent('cancel', false);
  }

});

Ext.reg('restEditForm', Sonia.rest.EditForm);

Sonia.rest.Grid = Ext.extend(Ext.grid.GridPanel, {

  restAddUrl: null,
  restEditUrlPattern: null,
  restRemoveUrlPattern: null,
  idField: null,
  searchField: null,
  editForm: null,
  editWindowWidth: 300,

  initComponent: function(){

    var restSelModel = new Ext.grid.RowSelectionModel({
      singleSelect: true
    });

    var restToolbar = new Ext.Toolbar({
      items: [
        {xtype: 'tbbutton', text: 'Add', scope: this, handler: this.showAddWindow},
        {xtype: 'tbbutton', text: 'Edit', scope: this, handler: this.showEditWindow},
        {xtype: 'tbbutton', text: 'Remove', scope: this, handler: this.removeItem},
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
      selModel: restSelModel,
      tbar: restToolbar,
      viewConfig: {
        forceFit: true
      },
      loadMask: true,
      listeners: {
        celldblclick: this.showEditWindow
      }
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.rest.Grid.superclass.initComponent.apply(this, arguments);
    
    // load data
    this.store.load();
  },

  showAddWindow: function(){
    var addWindow = new Sonia.rest.DetailWindow({
      items: [{
        id: 'addForm',
        xtype: this.editForm,
        listeners: {
          submit: {
            fn: function(item){

              var store = this.store;

              if ( debug ){
                console.debug( 'add item ' + item[this.idField] );
              }

              Ext.Ajax.request({
                url: this.restAddUrl,
                jsonData: item,
                method: 'POST',
                success: function(){
                  store.reload();
                  addWindow.close();
                },
                failure: function(){
                  alert( 'failure' );
                }
              });

            },
            scope: this
          },
          cancel: function(){
            addWindow.close();
          }
        }
      }]
    });

    addWindow.show();
  },

  showEditWindow: function(){
    if ( this.selModel.hasSelection() ){

      var data = this.selModel.getSelected().data;

      var editWindow = new Sonia.rest.DetailWindow({
        items: [{
          id: 'editForm',
          xtype: this.editForm,
          data: data,
          listeners: {
            submit: {
              fn: function(item){

                var store = this.store;
                var id = item[this.idField];
                var url = String.format(this.restEditUrlPattern, id);

                if ( debug ){
                  console.debug( 'update item ' + id );
                }

                Ext.Ajax.request({
                  url: url,
                  jsonData: item,
                  method: 'PUT',
                  success: function(){
                    store.reload();
                    editWindow.close();
                  },
                  failure: function(){
                    alert( 'failure' );
                  }
                });

              },
              scope: this
            },
            cancel: function(){
              editWindow.close();
            }
          }
        }]
      });

      editWindow.show();
    }
  },

  removeItem: function(){
    if ( this.selModel.hasSelection() ){
      var id = this.selModel.getSelected().data[this.idField];

      var store = this.store;
      var url = String.format( this.restRemoveUrlPattern, id );

      Ext.MessageBox.show({
        title: 'Remove Item',
        msg: 'Remove Item "' + id + '"?',
        buttons: Ext.MessageBox.OKCANCEL,
        icon: Ext.MessageBox.QUESTION,
        fn: function(result){

          if ( debug ){
            console.debug( 'remove item ' + id );
          }

          if ( result == 'ok' ){
            Ext.Ajax.request({
              url: url,
              method: 'DELETE',
              success: function(){
                store.reload();
              },
              failure: function(){
                alert( 'failure' );
              }
            });
          }
          
        }
      });

    }
  },

  reload: function(){
    this.store.reload();
  },

  search: function(value){
    if ( this.searchField != null ){
      this.store.filter(this.searchField, new RegExp('.*' + value + '.*'));
    }
  }

});

Ext.reg('restGrid', Sonia.rest.Grid);

Sonia.rest.DetailWindow = Ext.extend(Ext.Window, {

  initComponent: function(){
    var config = {
      layout:'fit',
      width: 300,
      autoScroll: true,
      closable: false,
      resizable: false,
      plain: true,
      border: false,
      modal: true
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.rest.DetailWindow.superclass.initComponent.apply(this, arguments);
  }

});

Ext.reg('restDetailWindow', Sonia.rest.DetailWindow);
