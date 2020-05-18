---
title: Java Client API
---

### Maven

To use the SCM-Manager Java Client API you have to configure the
SCM-Manager maven repository in your pom.xml:

```xml
<repositories>

  <repository>
    <id>packages.scm-manager.org</id>
    <name>scm-manager public repository</name>
    <url>https://packages.scm-manager.org/repository/public</url>
  </repository>

</repositories>
```

And you have to define the dependency to the api and one implementation:

```xml
<dependencies>

  <dependency>
    <groupId>sonia.scm.clients</groupId>
    <artifactId>scm-client-api</artifactId>
    <version>1.60</version>
  </dependency>

  <dependency>
    <groupId>sonia.scm.clients</groupId>
    <artifactId>scm-client-impl</artifactId>
    <version>1.60</version>
  </dependency>

</dependencies>
```

### Usage

First you have to create a session to your SCM-Manager instance:

```java
String url = "http://localhost:8080/scm";
String username = "scmadmin";
String password = "scmadmin";
ScmClientSession session = ScmClient.createSession(url, username, password);
```

After you have successfully created a client session you can nearly
execute every action which is available from the web interface. But do
not forget to close the session after you have finished your work:

```java
session.close();
```

### Examples

Create a new repository:

```java
Repository repository = new Repository();
repository.setName("scm-manager");
repository.setType("hg");
repository.setDescription("The easiest way to share and manage your Git, Mercurial and Subversion repositories over http.");

// set permissions for user sdorra to owner
List<Permission> permissions = new ArrayList<Permission>();
permissions.add(new Permission("sdorra", PermissionType.OWNER));
repository.setPermissions(permissions);

session.getRepositoryHandler().create(repository);
```

Get the last 20 commits of a repository:

```java
RepositoryClientHandler repositoryHandler = session.getRepositoryHandler();
// get the mercurial (hg) repository scm-manager
Repository repository = repositoryHandler.get("hg", "scm-manager");
ClientChangesetHandler changesetHandler = repositoryHandler.getChangesetHandler(repository);
// get 20 changesets started by 0
ChangesetPagingResult changesets = changesetHandler.getChangesets(0, 20);
for ( Changeset c : changesets ){
  System.out.println( c.getId() + ": " + c.getDescription() );
```

Print the content of a file in a repository:

```java
RepositoryClientHandler repositoryHandler = session.getRepositoryHandler();
// get the mercurial (hg) repository scm-manager
Repository repository = repositoryHandler.get("hg", "scm-manager");
ClientRepositoryBrowser browser = repositoryHandler.getRepositoryBrowser(repository);
BufferedReader reader = null;
try {
  // get the content of the file pom.xml at revision tip
  reader = new BufferedReader(new InputStreamReader(browser.getContent("tip", "pom.xml")));
  String line = reader.readLine();
  while ( line != null ){
    System.out.println( line );
    line = reader.readLine();
  }
} finally {
  if ( reader != null ){
    reader.close();
  }
}
```

Create a new user:

```java
User user = new User("tuser", "Test User", "test.user@test.net");
user.setPassword("test123");

session.getUserHandler().create( user );
```

Add a user to an existing group:

```java
GroupClientHandler groupHandler = session.getGroupHandler();
Group group = groupHandler.get("developers");
group.getMembers().add("tuser");
groupHandler.modify(group);
```
