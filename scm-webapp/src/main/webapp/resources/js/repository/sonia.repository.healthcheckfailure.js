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


Sonia.repository.HealthCheckFailure = Ext.extend(Ext.Panel, {
  
  title: 'Health check',
  linkTemplate: '<a target="_blank" href="{0}">{0}</a>',
  
  errorTitleText: 'Error',
  errorDescriptionText: 'Could not execute health check.',
  
  initComponent: function(){
    var items = [];
    
    if ( this.healthCheckFailures && this.healthCheckFailures.length > 0 ){
      for (var i=0; i<this.healthCheckFailures.length; i++){
        this.appendHealthCheckFailures(items,this.healthCheckFailures[i]);
      }
    }
    items.push({
      xtype: 'link',
      style: 'font-weight: bold',
      text: 'rerun health checks',
      handler: this.rerunHealthChecks,
      scope: this
    });
    
    var config = {
      title: this.title,
      padding: 5,
      bodyCssClass: 'x-panel-mc',
      layout: 'table',
      layoutConfig: {
        columns: 2,
        tableAttrs: {
          style: 'width: 80%;'
        }
      },
      defaults: {
        style: 'font-size: 12px'
      },
      items: items
    };
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.repository.HealthCheckFailure.superclass.initComponent.apply(this, arguments);
  },
  
  rerunHealthChecks: function(){
    var url = restUrl + 'repositories/' + this.repository.id + '/healthcheck.json';
    var el = this.el;
    var tid = setTimeout( function(){el.mask('Loading ...');}, 100);
    
    Ext.Ajax.request({
      url: url,
      method: 'POST',
      scope: this,
      success: function(){
        clearTimeout(tid);
        this.grid.reload(function(){
          this.grid.selectById(this.repository.id);
        }, this);
        el.unmask();
      },
      failure: function(result){
        clearTimeout(tid);
        el.unmask();
        main.handleFailure(
          result.status, 
          this.errorTitleText, 
          this.errorDescriptionText
        );
      }
    });
    
    this.grid.reload();
  },
  
  appendHealthCheckFailures: function(items, hcf){
    items.push({
      xtype: 'label',
      text: 'Summary:'
    },{
      xtype: 'label',
      text: hcf.summary
    });
    if ( hcf.url ){
      items.push({
        xtype: 'label',
        text: 'Url:'
      },{
        xtype: 'box',
        html: String.format(this.linkTemplate, hcf.url)
      });
    }
    if ( hcf.description ){
      items.push({
        xtype: 'label',
        text: 'Description:'
      },{
        xtype: 'label',
        text: hcf.description
      });
    }
    items.push({
      xtype: 'box',
      height: 10,
      colspan: 2
    });
  }
  
});

Ext.reg('repositoryHealthCheckFailurePanel', Sonia.repository.HealthCheckFailure);