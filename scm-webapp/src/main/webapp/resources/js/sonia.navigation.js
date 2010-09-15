/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


Ext.ns('Sonia.navigation');

Sonia.navigation.NavSection = Ext.extend(Ext.Panel, {

  data: null,

  initComponent: function(){

    var config = {
      frame: true,
      collapsible:true,
      style: 'margin: 5px',
      listeners: {
        afterrender: {
          fn: this.renderMenu,
          scope: this
        }
      }
    }

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.navigation.NavSection.superclass.initComponent.apply(this, arguments);

  },

  onItemClick: function(e, t){
    var target = Ext.get(t);
    var id = target.id;
    var prefix = this.id + '-nav-item-';
    if ( id != null && id.indexOf(prefix) == 0 ){
      var i = parseInt( id.substring( prefix.length ) );
      var fn = this.data[i].fn;
      if ( Ext.isFunction( fn ) ){
        fn();
      } else if ( debug ){
        console.debug('fn at "' +  this.data[i].label + '" is not a function');
      }
    }
  },

  renderMenu: function(){
    if ( Ext.isArray( this.data ) && this.data.length > 0 ){
      var links = [];
      for ( var i=0; i<this.data.length; i++ ){
        var item = this.data[i];
        var link = {
          tag: 'li',
          cls: 'nav-item',
          id: this.id + '-nav-item-' + i,
          html: item.label,
          style: 'cursor: pointer;'
        };
        links.push(link);  
      }

      var dh = Ext.DomHelper;
      var list = dh.append(this.body, {tag: 'ul', cls: 'nav-list'}, true);
      dh.append(list, links);
      list.on('click', this.onItemClick, this);
    }
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
    this.add({
      xtype: 'navSection',
      title: section.title,
      data: section.items
    });
  }

});

Ext.reg('navPanel', Sonia.navigation.NavPanel);