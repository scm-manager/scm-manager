Mercurial Subrepositories
-------------------------

In the following examples i will use the [scm-cli-client](command-line-client.md "wikilink")
to create the repositories, but you can also use the web interface to
create the repositories.

The best way to use subrepositories with scm-manager is the following.
Create a main repository and for each subrepository a mercurial
repository in scm-manager. Than add the subrepositories with the
complete url to the .hgsub file.

### Mercurial nested repositories

If you already have nested repositories, you have to redirect the nested
repository to a real scm-manager repository. This work is done by the
[scm-hgnested-plugin](https://bitbucket.org/sdorra/scm-hgnested-plugin "wikilink").
Install the
[scm-hgnested-plugin](https://bitbucket.org/sdorra/scm-hgnested-plugin "wikilink")
from the plugin-center (requires scm-manager version 1.10 or higher).
Create a repository for the main repository and for each nested
repository. Configure the
[scm-hgnested-plugin](https://bitbucket.org/sdorra/scm-hgnested-plugin "wikilink")
like [this](screenshots/scm-hgnested-plugin.png "wikilink").

### Further reading

-   <https://bitbucket.org/sdorra/scm-manager/issue/67/add-support-for-mercurial-subrepos>
-   <https://www.mercurial-scm.org/wiki/Subrepository>
-   <https://www.mercurial-scm.org/pipermail/mercurial-devel/2011-October/034728.html>
