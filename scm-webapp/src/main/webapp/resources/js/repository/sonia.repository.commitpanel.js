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

Sonia.repository.CommitPanel = Ext.extend(Ext.Panel, {
  
  repository: null,
  revision: null,
  
  // changeset
  changeset: null,
  
  // templates
  templateCommit: '<div class="scm-commit">\n\
                     <h1>Commit {id}</h1>\n\
                     <div class="left-side">\n\
                       <p>{description}</p>\n\
                       <p>\n\
                         <tpl for="author">\n\
                           {name}<tpl if="mail"> &lt;<a href="mailto:{mail}">{mail}</a>&gt;</tpl>\n\
                         </tpl>\n\
                       </p>\n\
                       <p>{date:formatTimestamp}</p>\n\
                     </div>\n\
                     <div class="right-side">\n\
                       <div class="changeset-tags">\n\
                         <tpl if="tags">\n\
                           <tpl for="tags">\n\
                             <span class="cs-tag"><a title="Tag {.}">{.}</a></span>\n\
                           </tpl>\n\
                         </tpl>\n\
                       </div>\n\
                       <div class="changeset-branches">\n\
                         <tpl if="branches">\n\
                           <tpl for="branches">\n\
                             <span class="cs-branch"><a title="Branch {.}">{.}</a></span>\n\
                           </tpl>\n\
                         </tpl>\n\
                       </div>\n\
                     </div>\n\
                   </div>',
  
  templateModifications: '<ul class="scm-modifications">\n\
               <tpl if="modifications.added"><tpl for="modifications.added"><li class="scm-added">{.}</li></tpl></tpl>\n\
               <tpl if="modifications.modified"><tpl for="modifications.modified"><li class="scm-modified">{.}</li></tpl></tpl>\n\
               <tpl if="modifications.removed"><tpl for="modifications.removed"><li class="scm-removed">{.}</li></tpl></tpl>\n\
             </ul>',
  
  // header panel
  commitPanel: null,
  diffPanel: null,
  
  initComponent: function(){
    this.commitPanel = new Ext.Panel({
      tpl: new Ext.XTemplate(this.templateCommit + this.templateModifications)
    });
    
    this.diffPanel = new Sonia.panel.SyntaxHighlighterPanel({
      syntax: 'diff',
      contentUrl: restUrl + 'repositories/' + this.repository.id + '/diff?revision=' + this.revision
    });
    
    var config = {
      bodyCssClass: 'x-panel-mc',
      padding: 10,
      autoScroll: true,
      items: [this.commitPanel, this.diffPanel]
    }
    
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.repository.CommitPanel.superclass.initComponent.apply(this, arguments);
    
    // loadChangeset
    this.loadChangeset();
  },
  
  update: function(changeset) {
    this.changeset = changeset;
    this.commitPanel.tpl.overwrite(this.commitPanel.body, this.changeset);
  },
  
  loadChangeset: function(){
    if (debug){
      console.debug('read changeset ' + this.revision);
    }
    
    Ext.Ajax.request({
      url: restUrl + 'repositories/' + this.repository.id + '/changeset/' + this.revision + '.json',
      method: 'GET',
      scope: this,
      success: function(response){
        var changeset = Ext.decode(response.responseText);
        this.update(changeset)
      },
      failure: function(result){
        main.handleFailure(
          result.status, 
          this.errorTitleText, 
          this.errorMsgText
        );
      }
    });
  }
  
});

Ext.reg('commitPanel', Sonia.repository.CommitPanel);