/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

Ext.ns('Sonia.group');

var addGroupWindow = null;

/** Sonia.GroupGrid **/
function addGroup(){
  console.debug( 'add group' );
  addGroupWindow = new Sonia.group.DetailWindow();
  addGroupWindow.show();
}

function removeGroup(){
  if (groupSelModel.hasSelection()){
    var group = groupSelModel.getSelected().data.name;
    console.debug( 'remove group ' + group );

    Ext.MessageBox.show({
      title: "Remove Group",
      msg: "Remove Group '" + group + "'?",
      buttons: Ext.MessageBox.OKCANCEL,
      icon: Ext.MessageBox.QUESTION,
      fn: function(result){
        if ( result == "ok" ){
          Ext.Ajax.request({
            url: 'api/rest/groups/' + group + ".json",
            method: 'DELETE',
            success: function(){
              groupStore.reload();
            },
            failure: function(){
              alert("ERROR!!!")
            }
          });
        }
      }
    });
  }
}

function editGroup(){
  if (groupSelModel.hasSelection()){
    var group = groupSelModel.getSelected().data;

    var store = new Ext.data.ArrayStore({
      fields: [ 'name' ]
    });

    Ext.each(group.members, function(g,i){
      store.add( [new Ext.data.Record( {name: g} )] );
    });

    var groupForm = new Sonia.group.AddForm({
      name: group.name,
      update: true,
      store: store
    });
    
    var win = new Sonia.group.DetailWindow({
      form: groupForm
    });
    win.show();
  }
}

var groupToolbar = new Ext.Toolbar({
  items: [
    {xtype: 'tbbutton', text: 'Add', handler: addGroup},
    {xtype: 'tbbutton', text: 'Edit', handler: editGroup},
    {xtype: 'tbbutton', text: 'Remove', handler: removeGroup},
    {xtype: 'tbseparator'},
    {xtype: 'label', text: 'Search: '},
    {xtype: 'textfield', id: 'searchfield', listeners: {
      specialkey: function(field, e){
        if (e.getKey() == e.ENTER) {
          var value = this.getValue();
          console.log( "Filter: " + value );
          // TODO filter by member
          groupStore.filter('name', new RegExp('.*' + value + '.*'));
        }
      }
    }}
  ]
});

var groupColModel = new Ext.grid.ColumnModel({
  columns: [
    {header: 'Name', sortable: true, width: 200, dataIndex: 'name'},
    {header: 'Members', sortable: true, dataIndex: 'members'}
  ]
});

var groupSelModel = new Ext.grid.RowSelectionModel({
  singleSelect: true
});

var groupStore = new Ext.data.JsonStore({
  url: 'api/rest/groups.json',
  root: 'groups',
  fields: [
    'name', "members"
  ],
  sortInfo: {
    field: 'name'
  }
});

groupStore.load();

Sonia.GroupGrid = Ext.extend(Ext.grid.GridPanel, {
  initComponent: function(){
    var config = {
      store: groupStore,
      colModel: groupColModel,
      selModel: groupSelModel,
      tbar: groupToolbar,
      viewConfig: {
        forceFit: true
      },
      loadMask: true,
      listeners: {
        celldblclick: editGroup
      }
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.GroupGrid.superclass.initComponent.apply(this, arguments);
  }
});

Ext.reg('groupGrid', Sonia.GroupGrid);

Sonia.group.AddForm = new Ext.extend(Ext.FormPanel, {

  containerWindow: null,
  store: null,
  name: '',
  update: false,

  initComponent: function(){
    if ( this.store == null ){
      this.store = new Ext.data.ArrayStore({
        fields: [ 'name' ]
      });
    }

    this.update = this.name != '';

    var config = {
      labelWidth: 80,
      autoHeight: true,
      //url: null,
      frame: true,
      title: 'Add Group',
      defaultType:'textfield',
      monitorValid: true,
      items:[{
        fieldLabel:'Name',
        name:'name',
        focus: ! this.update,
        value: this.name,
        readOnly: this.update,
        anchor: '100%',
        allowBlank: false
      },{
        fieldLabel: 'Members',
        xtype: 'fieldset',
        items: [{
          id: 'addMembersView',
          name: 'members',
          xtype: 'listview',
          columnResize: false,
          multiSelect: true,
          hideHeaders: true,
          store: this.store,
          columns: [{
            xtype: 'lvcolumn',
            header: 'Member',
            dataIndex: 'name'
          }]
        }]
      },{
        fieldLabel: 'Add Member',
        xtype: 'compositefield',
        items: [{
          id: 'addMemberField',
          name: 'addMember',
          width: '60%',
          xtype: 'textfield',
          scope: this,
          listeners: {
            specialkey: {
              fn: function(field, e){
                if (e.getKey() == e.ENTER) {
                  this.addMember();
                }
              },
              scope: this
            }
          }
        },{
          xtype: 'button',
          text: 'Add',
          scope: this,
          handler: this.addMember
        },{
          xtype: 'button',
          text: 'Del',
          scope: this,
          handler: this.removeSelectedMember
        }]
      }],

      buttons:[{
        text:'Ok',
        scope: this,
        formBind: true,
        handler: this.submit
      },{
        text: 'Cancel',
        scope: this,
        handler: this.close
      }]
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.group.AddForm.superclass.initComponent.apply(this, arguments);
  },

  addMemberBySpecialKey: function(field, e){
    if (e.getKey() == e.ENTER) {
      addMember();
    }
  },

  addMember: function(){
    var field = Ext.getCmp('addMemberField');
    var value = field.getValue();
    if ( value != '' ){
      this.store.add( [ new Ext.data.Record( {name: value}) ] );
      field.setValue('');
    }
  },

  removeSelectedMember: function(){
    var list = Ext.getCmp('addMembersView');
    var nodes = list.getSelectedIndexes();
    if ( nodes != null ){
      Ext.each(nodes, function(data, index){
        this.store.removeAt(data);
      }, this);
    }
  },

  submit: function(){
    var containerWindow = this.containerWindow;
    var form = this.getForm();
    var memberArray = [];
    this.store.each(function(data){
      memberArray.push( data.get('name') );
    });

    var name = form.findField('name').getValue();
    var group = { name: name, members: memberArray }

    var url = null;
    if ( this.update ){
      url = "api/rest/groups/" + this.name + ".json";
    } else {
      url = "api/rest/groups.json";
    }

    Ext.Ajax.request({
      url: url,
      jsonData: group,
      method: this.update ? "PUT" : "POST",
      success: function(){
        // TODO make this in a nice way
        groupStore.reload();

        if ( containerWindow ){
          containerWindow.close();
        }
      },
      failure: function(){
        alert( "failure" );
      }
    });

  },

  close: function(){
    if ( this.containerWindow ){
      this.containerWindow.close();
    }
  },

  load : function(data){
    this.store.loadData( data );
  }
  
});

Ext.reg('groupAddForm', Sonia.group.AddForm);

Sonia.group.DetailWindow = Ext.extend(Ext.Window, {

  form: null,

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

    var win = this;
    if ( this.form != null ){
      this.form.containerWindow = win;
      config.items = [ this.form ];
    } else {
      config.items = [{
        xtype: 'groupAddForm',
        containerWindow: win
      }];
    }

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.group.DetailWindow.superclass.initComponent.apply(this, arguments);
  }

});

Ext.reg('groupDetailWindow', Sonia.group.DetailWindow);
