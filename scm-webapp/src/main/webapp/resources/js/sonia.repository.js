/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

Ext.ns('Sonia.repository');

Sonia.repository.EditForm = Ext.extend(Sonia.rest.EditForm, {

  initComponent: function(){

    var update = this.data != null;

    var config = {
      title: 'Edit Repository',
      focusField: 'repositoryName',
      items:[
        { id: 'repositoryName', fieldLabel: 'Name', name: 'name', readOnly: update, allowBlank: false},
        {
         fieldLabel: 'Type',
         name: 'type',
         xtype: 'combo',
         readOnly: update,
         hiddenName : 'type',
         typeAhead: true,
         triggerAction: 'all',
         lazyRender: true,
         mode: 'local',
         editable: false,
         store: repositoryTypeStore,
         valueField: 'name',
         displayField: 'displayName',
         allowBlank: false
        },

        {fieldLabel: 'Contact', name: 'contact'},
        {fieldLabel: 'Description', name: 'description', xtype: 'textarea'}
      ]
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.repository.EditForm.superclass.initComponent.apply(this, arguments);
  }
  
});

Ext.reg('repositoryEditForm', Sonia.repository.EditForm);

Sonia.repository.Grid = Ext.extend(Sonia.rest.Grid, {

  initComponent: function(){

    var repositoryStore = new Sonia.rest.JsonStore({
      url: restUrl + 'repositories.json',
      root: 'repositories',
      fields: [ 'id', 'name', 'type', 'contact', 'description' ],
      sortInfo: {
        field: 'name'
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

    var config = {
      store: repositoryStore,
      colModel: repositoryColModel,
      idField: 'id',
      nameField: 'name',
      searchField: 'name',
      editForm: 'repositoryEditForm',
      restAddUrl: restUrl + 'repositories.json',
      restEditUrlPattern: restUrl + 'repositories/{0}.json',
      restRemoveUrlPattern: restUrl + 'repositories/{0}.json'
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.repository.Grid.superclass.initComponent.apply(this, arguments);
  }

});

Ext.reg('repositoryGrid', Sonia.repository.Grid);
