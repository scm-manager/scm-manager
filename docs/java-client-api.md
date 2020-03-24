Java Client API
---------------

### Maven

To use the SCM-Manager Java Client API you have to configure the
SCM-Manager maven repository in your pom.xml:

And you have to define the dependency to the api and one implementation:

### Usage

First you have to create a session to your SCM-Manager instance:

After you have successfully created a client session you can nearly
execute every action which is available from the web interface. But do
not forget to close the session after you have finished your work:

### Examples

Create a new repository:

Get the last 20 commits of a repository:

Print the content of a file in a repository:

Create a new user:

Add a user to an existing group:
