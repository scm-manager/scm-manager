WebHook Plugin
--------------

### Installation

-   Login in as administrator
-   Open Plugins
-   Install scm-webhook-plugin
-   Restart your applicationserver

### Usage

After the restart you should see a \"Webhooks\" tab for each repository.
You could now insert a new webhook for a repository. The url pattern is
the url of the remote webserver. It is possible to use placeholders in
the url:

-   \${repository.id} - the id of the current repository
-   \${repository.name} - the name of the current repository
-   \${first.id} - the if of the first changeset in the push
-   \${last.author.name} - the name of the author of the last changeset
    in the commit

If you enable the \"Execute on every commit\" checkbox the last and
first patterns are not available, but you could use the commit pattern
e.g:

-   \${changeset.id} - The id of the current changeset
-   \${changeset.author.name} - The name of the author of the current
    changeset

If you need more informations about the available patterns have a look
at the javadoc. Ever method which starts with a get could be used in a
pattern (Repository.getName() would be \${repository.name}):

-   [repository](http://docs.scm-manager.org/apidocs/latest/sonia/scm/repository/Repository.html "wikilink")
-   [changeset, first and
    last](http://docs.scm-manager.org/apidocs/latest/sonia/scm/repository/Changeset.html "wikilink")

Since version 1.4 of the webhook-plugin there is also a global
configuration at \"Config-\>General\". Global WebHooks are executed for
every repository.
