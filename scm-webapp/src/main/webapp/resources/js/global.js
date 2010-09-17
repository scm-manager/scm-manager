/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

var debug = true;
var state = null;

// functions called after login
var authCallbacks = [];

// config form panels
var repositoryConfigPanels = [];

var repositoryTypeStore = new Ext.data.JsonStore({
  id: 1,
  fields: [ 'displayName', 'name' ]
});

var restUrl = "api/rest/";

function loadState(s){
  state = s;
  console.debug( s );
  repositoryTypeStore.loadData(state.repositoryTypes);
  Ext.each(authCallbacks, function(callback){
    if ( Ext.isFunction(callback) ){
      callback(state);
    }
  });
}
