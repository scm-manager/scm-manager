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


Ext.form.Field.prototype.afterRenderExt = Ext.form.Field.prototype.afterRender;

Ext.override(Ext.form.Field, {

  afterRender : function() {
    if ( this.helpText != null ){
      this.renderHelp( this.helpText );
    }
    this.afterRenderExt.apply(this, arguments);
  },

  renderHelp : function(text){
    var div = this.el.up('div');
    var cls = this.getHelpButtonClass();

    var helpButton = div.createChild({
      tag: 'img',
      width : 16,
      height : 16,
      src: 'resources/images/help.gif',
      cls: cls
    });

    Ext.QuickTips.register({
      target : helpButton,
      title : '',
      text : text,
      enabled : true
    });
  },

  getHelpButtonClass: function(){
    var cls = null;

    switch ( this.getXType() ){
      case 'combo':
        if ( this.readOnly ){
          cls = 'scm-form-help-button';
        } else {
          cls = 'scm-form-combo-help-button';
        }
        break;
      case 'textarea':
        cls = 'scm-form-textarea-help-button';
        break;
      default:
        cls = 'scm-form-help-button';
    }

    return cls;
  }

});
