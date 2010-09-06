/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

var debug = true;

var state = null;

/*var repositoryTypes = [ ['Mercurial', 'hg'], ['Subversion','svn'], ['Git','git'] ];*/

var repositoryTypeStore = new Ext.data.JsonStore({
  id: 1,
  fields: [ 'displayName', 'name' ]
});

var restUrl = "api/rest/";