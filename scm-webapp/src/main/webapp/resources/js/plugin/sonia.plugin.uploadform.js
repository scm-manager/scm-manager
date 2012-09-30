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

Sonia.plugin.UploadForm = Ext.extend(Ext.FormPanel, {
  
  // TODO i18n
  
  emptyText: 'Select an Plugin-Package',
  uploadFieldLabel: 'Package',
  waitMsg: 'Uploading your package ...',
  btnUpload: 'Upload',
  btnReset: 'Reset',
  
  initComponent: function(){
    this.addEvents('success', 'failure');
    
    var config = {
      fileUpload: true,
      width: 500,
      frame: false,
      autoHeight: true,
      labelWidth: 50,
      bodyStyle: 'padding: 5px 0 0 10px;',
      defaults: {
        anchor: '95%',
        allowBlank: false,
        msgTarget: 'side'
      },
      items: [{
        xtype: 'fileuploadfield',
        id: 'form-file',
        emptyText: this.emptyText,
        fieldLabel: this.uploadFieldLabel,
        name: 'package',
        buttonText: '',
        buttonCfg: {
          iconCls: 'upload-icon'
        }
      }],
      buttons: [{
        text: this.btnUpload,
        handler: function(){
          if(this.getForm().isValid()){
            this.getForm().submit({
              url: restUrl + 'plugins/install-package.html',
              waitMsg: this.waitMsg,
              success: function(form, action){
                if (debug){
                  console.debug('upload success');
                }
                this.fireEvent('success');
              },
              failure: function(form, action){
                if (debug){
                  console.debug('upload failed');
                }
                this.fireEvent('failure');
              },
              scope: this
            });
          }
        },
        scope: this
      },{
        text: this.btnReset,
        handler: function(){
          this.getForm().reset();
        },
        scope: this
      }]
    };
    
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.plugin.UploadForm.superclass.initComponent.apply(this, arguments);
  }
  
});

Ext.reg('pluginPackageUploadForm', Sonia.plugin.UploadForm);