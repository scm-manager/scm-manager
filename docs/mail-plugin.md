scm-mail-plugin
===============

The mail plugin provides an central api for sending e-mails. This api
can be used by other plugins.

Configuration
-------------

The scm-mail-plugin provides a single place for the mail server
configurations at Config-\>General-\>Mail Settings.

API Usage
---------

First you have to add the dependency to your pom.xml e.g.:

But note you should use at least version 1.15 of scm-plugins as parent.

Now you can use the MailService class via injection e.g.:
