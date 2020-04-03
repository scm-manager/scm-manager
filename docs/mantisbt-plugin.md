# scm-mantisbt-plugin

The plugin enables the following features to integrate [MantisBT](https://www.mantisbt.org/) to SCM-Manager:

* Turn issue keys in changeset descriptions to links for MantisBT
* Updates a MantisBT issue if the issue key is found in a changeset description
* Change status of a MantisBT issue if the issue key and a status (e.g. resolved) word is found in the changeset description

**Note**: The issue keys must be defined with a 7 digit number (e.g. 0000001)
