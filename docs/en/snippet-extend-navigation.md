---
title: Snippet - Extend Navigation
---

```javascript
// add login callback
loginCallbacks.push(function(){
  // get the main navigation
  var navPanel = Ext.getCmp('navigationPanel');
  // insert the new navigation section at the
  // second position
  navPanel.insertSection(1, {
    title: 'My Links',
    links: [{
      label: 'Link 1',
      fn: function(){
        alert('Link 1');
      }
    },{
      label: 'Link 2',
      fn: function(){
        alert('Link 2');
      }
    }]
  });
});
```

[Complete source](https://bitbucket.org/sdorra/scm-code-snippets/src/tip/001-extend-navigation)
