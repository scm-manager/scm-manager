/**
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

Ext.ns('Sonia.navigation');

Sonia.navigation.NavSection = Ext.extend(Ext.Panel, {

  links: null,
  tpl: new Ext.XTemplate(
        '<ul class="scm-nav-list">',
        '<tpl for="links">',
            '<li class="scm-nav-item">',
                '<a id="{id}" class="scm-nav-item">{label}</a>',
            '</li>',
        '</tpl>',
        '</ul>'
      ),


  initComponent: function(){
    if ( this.links == null ){
      this.links = this.items;
    }
    if ( this.links == null ){
      this.links = [];
    }

    Ext.each(this.links, function(link){
      Ext.id( link );
    });

    var config = {
      frame: true,
      collapsible:true,
      style: 'margin: 5px'
    }

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.navigation.NavSection.superclass.initComponent.apply(this, arguments);
  },

  doAction: function(e, t){
    console.debug( t );
    e.stopEvent();
    Ext.each(this.links, function(navItem){
      if ( navItem.id == t.id && Ext.isFunction(navItem.fn) ){
        var scope = navItem.scope;
        if ( Ext.isObject( scope )){
          navItem.fn.call(scope);
        } else {
          navItem.fn();
        }
      }
    });
  },
  
  afterRender: function(){
    Sonia.navigation.NavSection.superclass.afterRender.apply(this, arguments);

    // create list items
    this.tpl.overwrite(this.body, {
        links: this.links
    });

    this.body.on('mousedown', this.doAction, this, {delegate:'a'});
    this.body.on('click', Ext.emptyFn, null, {delegate:'a', preventDefault:true});
  },


  addLink: function(link){
    Ext.id(link);
    this.links.push(link);
  },

  addLinks: function(links){
    if ( Ext.isArray(links) && links.length > 0 ){
      for ( var i=0; i<links.length; i++ ){
        this.addLink(links[i]);
      }
    } else {
      this.addLink(links);
    }
  },

  insertLink: function(pos, link){
    this.links.splice(pos, 0, link);
  },

  count: function(){
    return this.links.length;
  }

});

Ext.reg('navSection', Sonia.navigation.NavSection);

Sonia.navigation.NavPanel = Ext.extend(Ext.Panel, {

  sections: null,

  initComponent: function(){

    var config = {
      listeners: {
        afterrender: {
          fn: this.renderSections,
          scope: this
        }
      }
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.navigation.NavSection.superclass.initComponent.apply(this, arguments);
  },

  renderSections: function(){
    if ( this.sections != null ){
      this.addSections( this.sections );
    }
  },

  insertSection: function(pos, section){
    if ( debug ){
      console.debug('insert navsection ' + section.title + ' at ' + pos);
    }
    section.xtype = 'navSection';
    this.insert(pos, section);
    this.doLayout();
  },

  addSections: function(sections){
    if ( Ext.isArray( sections ) && sections.length > 0 ){
      for ( var i=0; i<sections.length; i++ ){
        this.addSection( sections[i] );
      }
    } else {
      this.addSection( sections );
    }
    this.doLayout();
  },

  addSection: function(section){
    if ( debug ){
      console.debug('add navsection ' + section.title);
    }
    section.xtype = 'navSection';
    this.add(section);
  },

  count: function(){
    return this.items.length;
  }

});

Ext.reg('navPanel', Sonia.navigation.NavPanel);
