/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


Ext.ns('Sonia.navigation');

Sonia.navigation.NavPanel = Ext.extend(Ext.Panel, {

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
    Sonia.navigation.NavPanel.superclass.initComponent.apply(this, arguments);

  },

  onItemClick: function(e, t){
    var target = Ext.get(t);
    var id = target.id;
    var prefix = this.id + '-nav-item-';
    if ( id != null && id.indexOf(prefix) == 0 ){
      var i = parseInt( id.substring( prefix.length ) );
      this.data[i].fn();
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

Ext.reg('navPanel', Sonia.navigation.NavPanel);