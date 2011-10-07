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

// RepositoryFormPanel
Sonia.repository.FormPanel = Ext.extend(Sonia.rest.FormPanel,{

  colGroupPermissionText: 'Is Group',
  colNameText: 'Name',
  colTypeText: 'Permissions',
  formTitleText: 'Settings',
  nameText: 'Name',
  typeText: 'Type',
  contactText: 'Contact',
  descriptionText: 'Description',
  publicText: 'Public',
  permissionText: 'Permission',
  errorTitleText: 'Error',
  updateErrorMsgText: 'Repository update failed',
  createErrorMsgText: 'Repository creation failed',

  // help
  nameHelpText: 'The name of the repository. This name will be part of the repository url.',
  typeHelpText: 'The type of the repository (e.g. Mercurial, Git or Subversion).',
  contactHelpText: 'Email address of the person who is responsible for this repository.',
  descriptionHelpText: 'A short description of the repository.',
  publicHelpText: 'Public repository, readable by everyone.',
  permissionHelpText: 'Manage permissions for a specific user or group.<br />\n\
  Permissions explenation:<br /><b>READ</b> = read<br /><b>WRITE</b> = read and write<br />\n\
  <b>OWNER</b> = read, write and also the ability to manage the properties and permissions',

  initComponent: function(){
    this.addEvents('preCreate', 'created', 'preUpdate', 'updated', 'updateFailed', 'creationFailed');
    Sonia.repository.FormPanel.superclass.initComponent.apply(this, arguments);
  },

  update: function(item){
    item = Ext.apply( this.item, item );
    if ( debug ){
      console.debug( 'update repository: ' + item.name );
    }
    var url = restUrl + 'repositories/' + item.id + '.json';
    var el = this.el;
    var tid = setTimeout( function(){el.mask('Loading ...');}, 100);

    this.fireEvent('preUpdate', item);

    Ext.Ajax.request({
      url: url,
      jsonData: item,
      method: 'PUT',
      scope: this,
      success: function(){
        if ( debug ){
          console.debug('update success');
        }
        this.fireEvent('updated', item);
        clearTimeout(tid);
        el.unmask();
        this.execCallback(this.onUpdate, item);
      },
      failure: function(result){
        this.fireEvent('updateFailed', item);
        clearTimeout(tid);
        el.unmask();
        main.handleFailure(
          result.status, 
          this.errorTitleText, 
          this.updateErrorMsgText
        );
      }
    });
  },
  
  getIdFromResponse: function(response){
    var id = null;
    var location = response.getResponseHeader('Location')
    if (location){
      var parts = location.split('/');
      id = parts[parts.length - 1];
    }
    return id;
  },

  create: function(item){
    if ( debug ){
      console.debug( 'create repository: ' + item.name );
    }
    var url = restUrl + 'repositories.json';
    var el = this.el;
    var tid = setTimeout( function(){el.mask('Loading ...');}, 100);

    this.fireEvent('preCreate', item);

    Ext.Ajax.request({
      url: url,
      jsonData: item,
      method: 'POST',
      scope: this,
      success: function(response){
        if ( debug ){
          console.debug('create success');
        }
        
        var id = this.getIdFromResponse(response);
        if (id){
          item.id = id;
        }
        
        this.fireEvent('created', item);
        this.getForm().reset();
        clearTimeout(tid);
        el.unmask();
        this.execCallback(this.onCreate, item);
      },
      failure: function(result){
        this.fireEvent('creationFailed', item);
        clearTimeout(tid);
        el.unmask();
        main.handleFailure(
          result.status, 
          this.errorTitleText, 
          this.createErrorMsgText
        );
      }
    });
  },

  cancel: function(){
    if ( debug ){
      console.debug( 'cancel form' );
    }
    Sonia.repository.setEditPanel( Sonia.repository.DefaultPanel );
  }

});

// register xtype
Ext.reg('repositoryForm', Sonia.repository.FormPanel);