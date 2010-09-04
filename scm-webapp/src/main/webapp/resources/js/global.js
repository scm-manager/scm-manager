/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

var repositoryTypes = [ ['Mercurial', 'hg'], ['Subversion','svn'], ['Git','git'] ];

var repositoryTypeStore = new Ext.data.ArrayStore({
  id: 1,
  fields: [ 'name', 'type' ],
  data: repositoryTypes
});

var restUrl = "api/rest/";