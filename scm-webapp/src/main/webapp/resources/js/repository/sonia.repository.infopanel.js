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

// default repository information panel

Sonia.repository.InfoPanel = Ext.extend(Ext.Panel, {

  linkTemplate: '<a target="_blank" href="{0}">{0}</a>',
  mailTemplate: '<a href="mailto: {0}">{0}</a>',
  actionLinkTemplate: '<a style="cursor: pointer;">{0}</a>',
  
  // text
  nameText: 'Name: ',
  typeText: 'Type: ',
  contactText: 'Contact: ',
  urlText: 'Url: ',
  changesetViewerText: 'Commits',

  initComponent: function(){
    
    var contact = '';
    if ( this.item.contact != null ){
      contact = String.format(this.mailTemplate, this.item.contact);
    }
    
    var items = [{
      xtype: 'label',
      text: this.nameText
    },{
      xtype: 'box',
      html: this.item.name
    },{
      xtype: 'label',
      text: this.typeText
    },{
      xtype: 'box',
      html: this.getRepositoryTypeText(this.item.type)
    },{
      xtype: 'label',
      text: this.contactText
    },{
      xtype: 'box',
      html: contact
    },{
      xtype: 'label',
      text: this.urlText
    },{
      xtype: 'box',
      html: String.format(this.linkTemplate, this.item.url)
    }];
    
    var config = {
      title: this.item.name,
      padding: 5,
      bodyCssClass: 'x-panel-mc',
      layout: 'table',
      layoutConfig: {
        columns: 2,
        tableAttrs: {
          style: 'width: 80%;'
        }
      },
      defaults: {
        style: 'font-size: 12px'
      },
      items: items
    }

    this.modifyDefaultConfig(config);
    
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.repository.InfoPanel.superclass.initComponent.apply(this, arguments);
  },
  
  modifyDefaultConfig: function(config){
    
  },
  
  getRepositoryUrlWithUsername: function(){
    var uri = this.item.url;
    if ( state.user.name != 'anonymous' ){
      var index = uri.indexOf("://");
      if ( index > 0 ){
        index += 3;
        uri = uri.substring(0, index) + state.user.name + "@" + uri.substring(index);
      }
    }
    return uri;
  },

  getRepositoryTypeText: function(t){
    var text = null;
    for ( var i=0; i<state.repositoryTypes.length; i++ ){
      var type = state.repositoryTypes[i];
      if ( type.name == t ){
        text = type.displayName + " (" + t + ")";
        break;
      }
    }
    return text;
  },
  
  createSpacer: function(){
    return {
      xtype: 'box',
      height: 10,
      colspan: 2
    };
  },
  
  createChangesetViewerLink: function(){
    return {
      xtype: 'link',
      style: 'font-weight: bold',
      text: this.changesetViewerText,
      handler: this.openChangesetViewer,
      scope: this
    };
  },

  createChangesetViewer: function(){
    return {
      id: 'repositoryChangesetViewerPanel;' + this.item.id,
      repository: this.item,
      xtype: 'repositoryChangesetViewerPanel',
      closable: true,
      autoScroll: true
    };
  },

  openChangesetViewer: function(changesetViewer){
    if ( changesetViewer == null ){
      changesetViewer = this.createChangesetViewer();
    }
    main.addTab(changesetViewer);
  }
  
});

// register xtype
Ext.reg('repositoryInfoPanel', Sonia.repository.InfoPanel);