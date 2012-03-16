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

Sonia.group.FormPanel = Ext.extend(Sonia.rest.FormPanel,{

  colMemberText: 'Member',
  titleText: 'Settings',
  nameText: 'Name',
  descriptionText: 'Description',
  membersText: 'Members',
  errorTitleText: 'Error',
  updateErrorMsgText: 'Group update failed',
  createErrorMsgText: 'Group creation failed',

  // help
  nameHelpText: 'Unique name of the group.',
  descriptionHelpText: 'A short description of the group.',
  membersHelpText: 'Usernames of the group members.',

  initComponent: function(){
    this.addEvents('preCreate', 'created', 'preUpdate', 'updated', 'updateFailed', 'creationFailed');
    Sonia.repository.FormPanel.superclass.initComponent.apply(this, arguments);
  },

  update: function(group){
    if ( debug ){
      console.debug( 'update group ' + group.name );
    }
    group = Ext.apply( this.item, group );

    // this.updateMembers(group);
    this.fireEvent('preUpdate', group);

    var url = restUrl + 'groups/' + group.name + '.json';
    var el = this.el;
    var tid = setTimeout( function(){el.mask('Loading ...');}, 100);

    Ext.Ajax.request({
      url: url,
      jsonData: group,
      method: 'PUT',
      scope: this,
      success: function(){
        if ( debug ){
          console.debug('update success');
        }
        this.fireEvent('updated', group);
        clearTimeout(tid);
        el.unmask();
        this.execCallback(this.onUpdate, group);
      },
      failure: function(result){
        this.fireEvent('updateFailed', group);
        clearTimeout(tid);
        el.unmask();
        main.handleRestFailure(
          result, 
          this.errorTitleText, 
          this.updateErrorMsgText
        );
      }
    });
  },

  create: function(item){
    if ( debug ){
      console.debug( 'create group: ' + item.name );
    }
    // item.type = 'xml';

    var url = restUrl + 'groups.json';
    var el = this.el;
    var tid = setTimeout( function(){el.mask('Loading ...');}, 100);
    
    this.fireEvent('preCreate', item);

    // this.updateMembers(item);

    Ext.Ajax.request({
      url: url,
      jsonData: item,
      method: 'POST',
      scope: this,
      success: function(){
        if ( debug ){
          console.debug('create success');
        }
        this.fireEvent('created', item);
        // this.memberStore.removeAll();
        this.getForm().reset();
        clearTimeout(tid);
        el.unmask();
        this.execCallback(this.onCreate, item);
      },
      failure: function(result){
        this.fireEvent('creationFailed', item);
        clearTimeout(tid);
        el.unmask();
        main.handleRestFailure(
          result, 
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
    Sonia.group.setEditPanel( Sonia.group.DefaultPanel );
  }

});

// register xtype
Ext.reg('groupFormPanel', Sonia.group.FormPanel);
