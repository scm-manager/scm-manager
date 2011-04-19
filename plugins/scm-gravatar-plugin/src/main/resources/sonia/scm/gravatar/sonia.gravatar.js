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


if (Sonia.repository.ChangesetViewerGrid){

  Sonia.repository.ChangesetViewerGrid.prototype.initComponentExt = Sonia.repository.ChangesetViewerGrid.prototype.initComponent;

  Ext.override(Sonia.repository.ChangesetViewerGrid, {

    gravatarSize: 32,
    gravatarNotFoundType: 'identicon',
    gravatarTemplate: '<img width="{1}" height="{1}" src="http://www.gravatar.com/avatar/{0}?s={1}&d={2}" alt="">',

    initComponent: function(){
      this.initComponentExt.apply(this, arguments);
      if ( debug ){
        console.debug('register gravatar plugin');
      }
      this.addColumn('properties', {
        id: 'gravatar',
        dataIndex: 'properties',
        renderer: this.renderGravatar,
        scope: this,
        width: this.gravatarSize + 8
      }, 0);
    },

    renderGravatar: function(properties){
      var hash = '00000000000000000000000000000000';
      if ( properties != null ){
        for (var i=0; i<properties.length; i++){
          var property = properties[i];
          if ( property.key == 'gravatar-hash' ){
            hash = property.value;
          }
        }
      }

      return String.format(
        this.gravatarTemplate,
        hash,
        this.gravatarSize,
        this.gravatarNotFoundType
      );
    }

  });

}