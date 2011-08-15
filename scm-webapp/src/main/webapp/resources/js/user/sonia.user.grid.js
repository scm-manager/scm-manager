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



Sonia.user.Grid = Ext.extend(Sonia.rest.Grid, {

  titleText: 'User Form',
  colNameText: 'Name',
  colDisplayNameText: 'Display Name',
  colMailText: 'Mail',
  colAdminText: 'Admin',
  colCreationDateText: 'Creation Date',
  colLastModifiedText: 'Last modified',
  colTypeText: 'Type',

  initComponent: function(){

    var userStore = new Sonia.rest.JsonStore({
      proxy: new Ext.data.HttpProxy({
        url: restUrl + 'users.json',
        disableCaching: false
      }),
      fields: [ 'name', 'displayName', 'mail', 'admin', 'creationDate', 'lastModified', 'type'],
      sortInfo: {
        field: 'name'
      }
    });

    var userColModel = new Ext.grid.ColumnModel({
      defaults: {
        sortable: true,
        scope: this,
        width: 125
      },
      columns: [
        {id: 'name', header: this.colNameText, dataIndex: 'name'},
        {id: 'displayName', header: this.colDisplayNameText, dataIndex: 'displayName', width: 250},
        {id: 'mail', header: this.colMailText, dataIndex: 'mail', renderer: this.renderMailto, width: 200},
        {id: 'admin', header: this.colAdminText, dataIndex: 'admin', renderer: this.renderCheckbox, width: 50},
        {id: 'creationDate', header: this.colCreationDateText, dataIndex: 'creationDate', renderer: Ext.util.Format.formatTimestamp},
        {id: 'lastModified', header: this.colLastModifiedText, dataIndex: 'lastModified', renderer: Ext.util.Format.formatTimestamp},
        {id: 'type', header: this.colTypeText, dataIndex: 'type', width: 80}
      ]
    });

    var config = {
      store: userStore,
      colModel: userColModel,
      listeners: {
        fallBelowMinHeight: {
          fn: this.onFallBelowMinHeight,
          scope: this
        }
      }
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.user.Grid.superclass.initComponent.apply(this, arguments);
  },

  onFallBelowMinHeight: function(height, minHeight){
    var p = Ext.getCmp('userEditPanel');
    this.setHeight(minHeight);
    var epHeight = p.getHeight();
    p.setHeight(epHeight - (minHeight - height));
    // rerender
    this.doLayout();
    p.doLayout();
    this.ownerCt.doLayout();
  },

  selectItem: function(item){
    if ( debug ){
      console.debug( item.name + ' selected' );
    }
    
    Sonia.History.append(item.name);
    
    var panel = new Sonia.user.FormPanel({
      item: item,
      region: 'south',
      title: this.titleText,
      padding: 5,
      onUpdate: {
        fn: this.reload,
        scope: this
      },
      onCreate: {
        fn: this.reload,
        scope: this
      }
    });
    if ( item.type == 'xml' ){
      panel.getForm().setValues([
        {id: 'password', value: dummyPassword},
        {id: 'password-confirm', value: dummyPassword}
      ]);
    }
    Sonia.user.setEditPanel(panel);
  }

});

// register xtype
Ext.reg('userGrid', Sonia.user.Grid);
