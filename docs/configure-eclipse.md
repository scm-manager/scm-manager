There are multiple approaches to using Maven projects within Eclipse. If
you use an approach that isn\'t listed here, feel free to add it.

Use Maven to generate Eclipse project files
-------------------------------------------

1.  First, [build scm-manager from
    source](build-from-source "wikilink").
2.  Open a shell to the root of your scm-manager source.
3.  Run the following command:\\\\
4.  Run the following command:\\\\
5.  Open Eclipse using the specified workspace.
6.  In Eclipse, select the \"File\", \"Import\...\" menu.
7.  Select the \"General\", \"Existing Projects into Workspace\" item
    and click \"Next\".
8.  Click \"Browse\" and select the root of your scm-manager source.
9.  All projects will be selected by default. Click \"Finish\".

### Troubleshooting

You may encounter an error during the \"eclipse:eclipse\" step that
looks like this:

This error is caused by a bug in version 2.7 and 2.8 of the Maven
eclipse plugin. See below for one workaround. Other discussion of this
issue can be found
[here](http://forums.atlassian.com/thread.jspa?threadID=34952&tstart=1 "wikilink").

1.  Add the following line within the <settings> element of your
    \~/.m2/settings.xml [file:\\\\](file:\\){{{

<usePluginRegistry>true</usePluginRegistry> }}}

1.  Add the file \~/.m2/plugin-registry.xml with the following contents:

If you have .project files in any parent directory, the Eclipse import
step will not search beneath that parent for any further projects.
Delete the .project file in the parent and try the import again.

If you get errors in Eclipse about not finding M2\_REPO, it means that
the eclipse:configure-workspace step didn\'t work. One cause for this
could be using \"\~\" or other special characters in your workspace path
that the plugin doesn\'t resolve properly. Either re-run
eclipse:configure-workspace, or manually create a classpath variable
named M2\_REPO pointing to the root of your local maven repository
directory.

If you get compilation errors on JAXB or ServiceLoader, your Eclipse may
not be configured to use Java 6.
