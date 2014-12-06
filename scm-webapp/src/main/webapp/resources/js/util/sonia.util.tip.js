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
   
Sonia.util.Tip = Ext.extend(Ext.BoxComponent, {

  tpl: new Ext.XTemplate('\
    <div id="" class="x-tip" style="position: inherit; visibility: visible;">\
      <div class="x-tip-tl">\
        <div class="x-tip-tr">\
          <div class="x-tip-tc">\
            <div class="x-tip-header x-unselectable">\
              <span class="x-tip-header-text"></span>\
            </div>\
          </div>\
        </div>\
      </div>\
      <div class="x-tip-bwrap">\
        <div class="x-tip-ml">\
          <div class="x-tip-mr">\
            <div class="x-tip-mc">\
              <div class="x-tip-body" style="height: auto; width: auto;">\
                {content}\
              </div>\
            </div>\
          </div>\
        </div>\
        <div class="x-tip-bl x-panel-nofooter">\
          <div class="x-tip-br">\
            <div class="x-tip-bc"></div>\
          </div>\
        </div>\
      </div>\
    </div>'),
  
  constructor: function(config) {
    config = config || {};
    var cl = 'scm-tip';
    if (config['class']){
      cl += ' ' + config['class'];
    }
    config.xtype = 'box';
    this.html = this.tpl.apply({content: config.content});
    Sonia.util.Tip.superclass.constructor.apply(this, arguments);
  }

});

// register xtype
Ext.reg('scmTip', Sonia.util.Tip);