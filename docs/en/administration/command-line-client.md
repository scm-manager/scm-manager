---
title: Command line client
---

You can download the command line client from
[here](http://www.scm-manager.org/download/) (the scm-cli-client).

### Examples

**1\. Store username, password and server url**

```bash
$ java -jar scm-cli-client-1.47-jar-with-dependencies.jar --user scmadmin --password madmin --server http://localhost:8080/scm store-config

store config
```

**2\. List all repositories**

```bash
$ java -jar scm-cli-client-1.47-jar-with-dependencies.jar list-repositories

ID:             fbb64701-6dd3-4847-8588-26f693736961
Name:           scm
Type:           hg
E-Mail:         s.sdorra@gmail.com
Description:    SCM-Manager
Public:         false
Creation-Date:  2011-06-03 16:13:19
Last-Modified:  2011-06-03 16:15:38
URL:            http://localhost:8080/scm/hg/scm
Permissions:
  WRITE - sdorra (Group: false)
```

**3\. Create a new user**

```bash
$ java -jar scm-cli-client-1.47-jar-with-dependencies.jar create-user --name test --display-name "Test User" --mail "test@scm-manager.org" --password secret

Name:           test
Display Name:   Test User
Type:           xml
E-Mail:         test@scm-manager.org
Administrator:  false
Creation-Date:  
Last-Modified:  
```

**4\. Add write permission for user test to repository scm**

```bash
$ java -jar scm-cli-client-1.47-jar-with-dependencies.jar add-permission fbb64701-6dd3-4847-8588-26f693736961 --name test -t WRITE

ID:             fbb64701-6dd3-4847-8588-26f693736961
Name:           scm
Type:           hg
E-Mail:         s.sdorra@gmail.com
Description:    SCM-Manager
Public:         false
Creation-Date:  2011-06-03 16:13:19
Last-Modified:  2011-06-03 16:15:38
URL:            http://localhost:8080/scm/hg/scm
Permissions:
  WRITE - sdorra (Group: false)
  WRITE - test (Group: false)
```
