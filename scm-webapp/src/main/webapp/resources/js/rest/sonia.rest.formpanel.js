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

Sonia.rest.FormPanel = Ext.extend(Ext.FormPanel,{

  okText: 'Ok',
  cancelText: 'Cancel',
  addText: 'Add',
  removeText: 'Remove',

  item: null,
  onUpdate: null,
  onCreate: null,

  initComponent: function(){
    var config = {
      bodyCssClass: 'x-panel-mc',
      padding: 5,
      labelWidth: 100,
      defaults: {width: 240},
      autoScroll: true,
      monitorValid: true,
      defaultType: 'textfield',
      buttonAlign: 'center',
      footerCfg: {
        cls: 'x-panel-mc'
      },
      buttons: [
        {text: this.okText, formBind: true, scope: this, handler: this.submit},
        {text: this.cancelText, scope: this, handler: this.cancel}
      ]
    }

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.rest.FormPanel.superclass.initComponent.apply(this, arguments);

    if ( this.item != null ){
      this.loadData(this.item);
    }
  },

  loadData: function(item){
    this.item = item;
    var data = {success: true, data: item};
    this.getForm().loadRecord(data);
  },

  submit: function(){
    if ( debug ){
      console.debug( 'form submitted' );
    }
    var item = this.getForm().getFieldValues();
    if ( this.item != null ){
      this.update(item);
    } else {
      this.create(item);
    }
  },
  
  cancel: function(){
    if ( debug ){
      console.debug( 'reset form' );
    }
    this.getForm().reset();
  },

  execCallback: function(obj, item){
    if ( Ext.isFunction( obj ) ){
      obj(item);
    } else if ( Ext.isObject( obj )){
      obj.fn.call( obj.scope, item );
    }
  },

  update: function(item){
    if ( debug ){
      console.debug( 'update item: ' );
      console.debug( item );
    }
  },

  create: function(item){
    if (debug){
      console.debug( 'create item: ' );
      console.debug( item );
    }
  }

});

