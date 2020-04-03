# Snippet: Extend global configuration

```javascript
// register the new configuration form
registerGeneralConfigPanel({
  // the xtype of the form should be configForm
  xtype : 'configForm',
  // title of the form
  title : 'My Configuration',
  // array of formular fields
  items : [{
    /**
     * xtype of the formular field. For a 
     * list of the xtype's have a look at
     * http://docs.sencha.com/ext-js/3-4/#!/api/Ext.Component
     **/
    xtype : 'textfield',
    // label of the field
    fieldLabel : 'Config name',
    // name of the field
    name : 'fomular-field-name',
    // help for this field
    helpText: 'Help for this field.',
    // allow blank values
    allowBlank : true
  }],

  /**
   * this method is called when the form is submitted.
   * values - the values of the formular
   **/
  onSubmit: function(values){
    // do something
  },

  /**
   * this method is called when the form is load
   * el - the element of the formular
   **/
  onLoad: function(el){
    // do something
  }
});
```

[Complete source](https://bitbucket.org/sdorra/scm-code-snippets/src/tip/003-extend-global-config)
