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


// extended information panel

Sonia.repository.ExtendedInfoPanel = Ext.extend(Sonia.repository.InfoPanel,{
  
  checkoutTemplate: null,
  
  // text
  checkoutText: 'Checkout: ',
  
  // TODO i18n
  repositoryBrowserText: 'Source',
  
  enableRepositoryBrowser: true,
  enableChangesetViewer: true,
  
  modifyDefaultConfig: function(config){
    var items = config.items;
    if ( items == null ){
      items = [];
    }
    items.push({
      xtype: 'label',
      text: this.checkoutText
    },{
      xtype: 'box',
      html: String.format(
              this.checkoutTemplate, 
              this.getRepositoryUrlWithUsername()
            )
    },this.createSpacer());
    
    var box = [];
    if ( this.enableChangesetViewer ){
      box.push(this.createChangesetViewerLink());
      if (this.enableRepositoryBrowser){
        box.push({
          xtyle: 'box',
          html: ', ',
          width: 8
        });
      }
    }
    
    if (this.enableRepositoryBrowser){
      box.push(this.createRepositoryBrowserLink());
    }
    
    items.push({
      xtype: 'panel',
      colspan: 2,
      layout: 'column',
      items: box
    });
  },
  
  createRepositoryBrowserLink: function(){
    return {
      xtype: 'link',
      style: 'font-weight: bold',
      text: this.repositoryBrowserText,
      handler: this.openRepositoryBrowser,
      scope: this
    };
  },
  
  createRepositoryBrowser: function(){
    return {
      id: 'repositorybrowser-' + this.item.id,
      xtype: 'repositoryBrowser',
      repository: this.item,
      closable: true
    }
  },
  
  openRepositoryBrowser: function(browser){
    if ( browser == null ){
      browser = this.createRepositoryBrowser();
    }
    main.addTab(browser);
  }
  
});

// register xtype
Ext.reg("repositoryExtendedInfoPanel", Sonia.repository.ExtendedInfoPanel);