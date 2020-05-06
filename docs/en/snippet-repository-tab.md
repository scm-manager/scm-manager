---
title: Snippet - Add tab to repository configuration
---

```javascript
/** 
 * Register extjs namespace for the plugin.
 * http://docs.sencha.com/ext-js/3-4/#!/api/Ext-method-ns
 */
Ext.ns('Sonia.snippets');

Sonia.snippets.MyPanel = Ext.extend(Ext.Panel, {

  initComponent: function(){
    var config = {
      // Title of the panel
      title: 'My Panel'
    }

    /**
     * The apply method merges the initialConfig object with the config object.
     * The initialConfig object is the config object from the parent panel 
     * (in this case Ext.Panel).
     * http://docs.sencha.com/ext-js/3-4/#!/api/Ext-method-apply
     */
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.snippets.MyPanel.superclass.initComponent.apply(this, arguments);
  }

});

/**
 * Register xtype of the panel for later use and lazy initialization.
 * http://docs.sencha.com/ext-js/3-4/#!/api/Ext-method-reg
 */
Ext.reg("myPanel", Sonia.snippets.MyPanel);

/**
 * Register a listener which is called, after repository is selected in the 
 * web interface. The listener passes the selected repository and an array
 * of panels as argument.
 */ 
Sonia.repository.openListeners.push(function(repository, panels){
  
  /**
   * Append the new panel to the panels array
   */
  panels.push({
    // registerd xtype for the panel
    xtype: 'myPanel'
  });
});
```

[Complete source](https://bitbucket.org/sdorra/scm-code-snippets/src/tip/002-repository-tab)
