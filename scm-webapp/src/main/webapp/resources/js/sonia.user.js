/**
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

// register namespace
Ext.ns('Sonia.user');

// UserGrid
Sonia.user.Grid = Ext.extend(Ext.grid.GridPanel, {

  mailtoTemplate: '<a href="mailto: {0}">{0}</a>',

  initComponent: function(){

    var selectionModel = new Ext.grid.RowSelectionModel({
      singleSelect: true
    });

    selectionModel.on({
      selectionchange: {
        scope: this,
        fn: this.selectionChanged
      }
    });

    var userStore = new Sonia.rest.JsonStore({
      url: restUrl + 'users.json',
      root: 'users',
      fields: [ 'name', 'displayName', 'mail', 'type'],
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
        {id: 'name', header: 'Name', dataIndex: 'name'},
        {id: 'displayName', header: 'Display Name', dataIndex: 'displayName', width: 250},
        {id: 'mail', header: 'Mail', dataIndex: 'mail', renderer: this.renderMailto, width: 200},
        {id: 'type', header: 'Type', dataIndex: 'type', width: 80}
      ]
    });

    var config = {
      loadMask: true,
      store: userStore,
      colModel: userColModel,
      sm: selectionModel
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.user.Grid.superclass.initComponent.apply(this, arguments);

    // load data
    if ( debug ){
      console.debug( 'load user list' );
    }
    userStore.load();
  },

  reload: function(){
    if ( debug ){
      console.debug('reload store');
    }
    this.store.load();
  },

  selectionChanged: function(sm){
    var selected = sm.getSelected();
    if ( selected ){
      if ( debug ){
        console.debug( selected.data.name + ' selected' );
      }
    }
  },

  renderMailto: function(mail){
    return String.format( this.mailtoTemplate, mail );
  }

});

// register xtype
Ext.reg('userGrid', Sonia.user.Grid);