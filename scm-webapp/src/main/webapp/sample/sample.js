/**
 * Copyright (c) 2009, Sebastian Sdorra
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

Ext.onReady(function(){
  
  var repos = [
    ['sonia.lib', 'SONIA Libraries', 's.sdorra@gmail.com', 'Die gesamelten Werke der CSIT'],
    ['scm', 'SCM', 's.sdorra@gmail.com', 'SONIA SCM SourceCode Manager'],
    ['scm-git', 'SCM Git Mirror', 's.sdorra@gmail.com', 'SONIA SCM SourceCode Manager, Git Mirror']
  ];

  var store = new Ext.data.ArrayStore({
    fields: [
      {name: 'id'},
      {name: 'name'},
      {name: 'mail'},
      {name: 'description'}
    ]
  });

  store.loadData(repos);

  var grid = {
    xtype: 'grid',
    split: true,
    region: 'center',
    store: store,
    autoExpandColumn: 'description',
    columns: [
      {id: 'id', header: 'Id', dataIndex: 'id'},
      {id: 'name', header: 'Name', dataIndex: 'name'},
      {id: 'mail', header: 'E-Mail', dataIndex: 'mail'},
      {id: 'description', header: 'Description', dataIndex: 'description'}
    ],
    viewConfig: {
      forceFit: true,
      enableRowBody: true,
      showPreview: true
    }
  }

  var form = {
    xtype: 'form',
    padding: 5,
    autoScroll: true,
    region: 'south',
    title: 'Repository Form',
    items: [
      {xtype: 'textfield', fieldLabel: 'Id', name: 'id'},
      {xtype: 'textfield', fieldLabel: 'Name', name: 'name'},
      {xtype: 'textfield', fieldLabel: 'E-Mail', name: 'mail'},
      {xtype: 'textfield', fieldLabel: 'Description', name: 'description'},
    ],
    buttonAlign: 'center',
    buttons: [
      {text: 'Ok', scope: this},
      {text: 'Cancel', scope: this}
    ]
  };

  var panel = {
    id: 'welcome',
    xtype: 'panel',
    title: 'Welcome',
    layout: 'border',
    hideMode: 'offsets',
    bodyCssClass: 'x-panel-mc',
    enableTabScroll: true,
    region:'center',
    autoScroll: true,  
    items: [
      grid, {
        id: 'bottom-preview',
        layout: 'fit',
        items: [form],
        height: 250,
        split: true,
        border: false,
        region: 'south'
      }
    ]
  }

  new Ext.Viewport({
    layout: 'border',
    items: [
      new Ext.BoxComponent({
        region: 'north',
        id: 'north-panel',
        contentEl: 'north',
        height: 75
      }), {
        region: 'west',
        id: 'west',
        title: 'Navigation',
        xtype: 'panel',
        split: true,
        width: 200,
        minSize: 175,
        maxSize: 400,
        collapsible: true,
        margins: '0 0 0 5'
      },
      new Ext.BoxComponent({
        region: 'south',
        id: 'south-panel',
        contentEl: 'south',
        height: 16,
        margins: '2 2 2 5'
      }),{
        xtype: 'tabpanel',
        region: 'center',
        deferredRender: false,
        activeTab: 0,
        items: [panel]
      }
  ]});

});
