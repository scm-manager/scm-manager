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

Sonia.action.ExceptionWindow = Ext.extend(Ext.Window,{
  
  title: null,
  message: null,
  stacktrace: null,
  icon: Ext.MessageBox.ERROR,

  // labels
  okText: 'Ok',
  detailsText: 'Details',
  exceptionText: 'Exception',
  
  initComponent: function(){
    var config = {
      title: this.title,
      width: 300,
      height: 110,
      closable: true,
      resizable: true,
      plain: true,
      border: false,
      modal: true,
      bodyCssClass: 'x-window-dlg',
      items: [{
        xtype: 'panel',
        height: 45,
        bodyCssClass: 'x-panel-mc x-dlg-icon',
        html: '<div class="ext-mb-icon ' + this.icon + '"></div><div class="ext-mb-content"><span class="ext-mb-text">' + this.message + '</span></div>'
      },{
        id: 'stacktraceArea',
        xtype: 'textarea',
        editable: false,
        fieldLabel: this.exceptionText,
        value: this.stacktrace,
        width: '98%',
        height: 260,
        hidden: true
      }],
      buttons: [{
        text: this.okText,
        scope: this,
        handler: this.close
      },{
        id: 'detailButton',
        text: this.detailsText,
        scope: this,
        handler: this.showDetails        
      }]
    }
    
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.action.ChangePasswordWindow.superclass.initComponent.apply(this, arguments);
  },
  
  showDetails: function(){
    Ext.getCmp('detailButton').setDisabled(true);
    Ext.getCmp('stacktraceArea').setVisible(true);
    this.setWidth(640);
    this.setHeight(380);
    this.center();
  }
  
});