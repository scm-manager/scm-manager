/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

Ext.ns("Sonia.sample");

Sonia.sample.EditForm = new Ext.extend(Sonia.rest.EditForm, {

  initComponent: function(){

    if ( this.store == null ){
      this.store = new Ext.data.ArrayStore({
        fields: [ 'name' ]
      });
    }

    var config = {
      items:[{
        fieldLabel:'Name',
        name:'name',
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
      }]
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.sample.EditForm.superclass.initComponent.apply(this, arguments);
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

Ext.reg('sampleEditForm', Sonia.sample.EditForm);