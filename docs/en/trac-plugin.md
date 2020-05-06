# Trac Plugin

The plugin enables the following features to integrate SCM-Manager to [Trac](http://trac.edgewall.org/):

* Turn ticket ids in changeset descriptions to links for [Trac](http://trac.edgewall.org/)
* Updates a [Trac](http://trac.edgewall.org/) ticket if the ticket id is found in a changeset description
* Close a [Trac](http://trac.edgewall.org/) ticket if the ticket id and a resolution key word (fixed, invalid, wontfix, duplicate and worksforme) is found in the changeset description

The plugin needs an installed [Trac XML-RPC Plugin](http://trac-hacks.org/wiki/XmlRpcPlugin) and each user which  should be able to update tickets via SCM-Manager needs the XML_RPC permission in Trac.

