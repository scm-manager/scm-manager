/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

Ext.ns('Sonia.group');

Sonia.group.EditForm = new Ext.extend(Sonia.rest.EditForm, {

  initComponent: function(){
    this.store = new Ext.data.ArrayStore({
      fields: [ 'name' ]
    });

    var update = this.data != null;

    var config = {
      title: 'Edit Group',
      listeners: {
        afterrender: function(){
          if ( ! update ){
            Ext.getCmp('nameField').focus(true, 500);
          }
        }
      },
      items:[{
        id: 'nameField',
        fieldLabel:'Name',
        name:'name',
        anchor: '100%',
        allowBlank: false,
        readOnly: update
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
      }]
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.group.EditForm.superclass.initComponent.apply(this, arguments);
  },

  load: function(item){
    var members = item.members;
    for (var i=0; i<members.length; i++){
      this.store.add( [ new Ext.data.Record( {name: members[i]}) ] );
    }
    this.getForm().loadRecord({success: true, data: item});
  },

  getItem: function(form){

    var memberArray = [];
    this.store.each(function(data){
      memberArray.push( data.get('name') );
    });

    var name = form.findField('name').getValue();
    var group = { name: name, members: memberArray }

    return group;
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
  }

});

Ext.reg('groupEditForm', Sonia.group.EditForm);

Sonia.group.Grid = Ext.extend(Sonia.rest.Grid, {

  initComponent: function(){

    var groupColModel = new Ext.grid.ColumnModel({
      columns: [
        {header: 'Name', sortable: true, width: 200, dataIndex: 'name'},
        {header: 'Members', sortable: true, dataIndex: 'members'}
      ]
    });

    var groupStore = new Sonia.rest.JsonStore({
      url: restUrl + 'groups.json',
      root: 'groups',
      fields: [
        'name', 'members'
      ],
      sortInfo: {
        field: 'name'
      }
    });

    var config = {
      store: groupStore,
      colModel: groupColModel,
      idField: 'name',
      searchField: 'name',
      editForm: 'groupEditForm',
      restAddUrl: restUrl + 'groups.json',
      restEditUrlPattern: restUrl + 'groups/{0}.json',
      restRemoveUrlPattern: restUrl + 'groups/{0}.json'
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.group.Grid.superclass.initComponent.apply(this, arguments);
  }

});

Ext.reg('groupGrid', Sonia.group.Grid);