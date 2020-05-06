---
title: SCM-Manager v2 Test Cases
---

Describes the expected behaviour for SCMM v2 REST Resources using manual tests.

The following states general test cases per HTTP Method and en expected return code as well as exemplary curl calls.
Resource-specifics are stated 

## Test Cases

### GET

- Collection Resource (e.g. `/users`)
    - Without parameters -> 200
    - Parameters
        - `?pageSize=1` -> Only one embedded element, pageTotal reflects the correct number of pages, `last` link points to last page.
        - `?pageSize=1&page=1` -> `next` link points to page 0 ; `prev` link points to page 2
        - `?sortBy=admin` -> Sorted by `admin` field of embedded objects
        - `?sortBy=admin&desc=true` -> Invert sorting
- Individual Resource (e.g. `/users/scmadmin`)
    - Exists  -> 200
    - Not Existings -> 404
    - Known Field (e.g. `?fields=name`) returns only name field
    - Unknown field (e.g. `?fields=nam`) returns empty object
- without permission (individual and collection (TODO)) -> 401

### POST

- not existing -> 204
- existing -> 409
- without permission -> 401

### PUT

- existing -> 204
    - lastModified is updated
    - lastModified & creationDate cannot be overwritten by client
- not exist -> 404
- Change ID / Name (the one from the URL in the body) -> 400
- Partial PUT (Set only one field, for example) -> Set all other fields to null or return 400?
- without permission -> 401
- Change unmodifiable fields
  - ID/Name --> 400
  - creationDate, lastModified --> 200 is liberally ignored
  - Additional unmodifiable fields per resource, see examples

### DELETE

- existing -> 204
- not existing -> 204
- without permission -> 401

## Exemplary calls & Resource specific test cases

In order to extend those tests to other Resources, have a look at the rest docs. Note that the Content Type is specific to each resource as well.

After calling `mvn -pl scm-webapp compile -P doc` the docs are available at `scm-webapp/target/restdocs/index.html`.

### Users

#### GET

##### Collections 

```bash
curl -vu scmadmin:scmadmin "http://localhost:8081/scm/api/v2/users?sortBy=admin&desc=true"
```

##### Individual

```bash
curl -vu scmadmin:scmadmin "http://localhost:8081/scm/api/v2/users/scmadmin?fields=name,_links"
```

#### POST

```bash
curl -vu scmadmin:scmadmin --data '{
  "properties": null,
  "active": true,
  "admin": false,
  "creationDate": 1527510477501,
  "displayName": "xyz",
  "lastModified": null,
  "mail": "x@abcde.cd",
  "name": "xyz",
  "password": "pwd123",
  "type": "xml"
  }' \
   --header "Content-Type: application/vnd.scmm-user+json;v=2"  http://localhost:8081/scm/api/v2/users/
```

#### PUT

- Change unmodifiable fields
  - type? -> can be overwritten right now

```bash
curl -X PUT -vu scmadmin:scmadmin --data '{
  "properties": null,
  "active": true,
  "admin": false,
  "creationDate": 1527510477501,
  "displayName": "xyz",
  "lastModified": null,
  "mail": "x@abcde.cd",
  "name": "xyz",
  "password": "pwd123",
  "type": "xml"
  }' \
   --header "Content-Type: application/vnd.scmm-user+json;v=2"  http://localhost:8081/scm/api/v2/users/xyz 
```

#### DELETE

```bash
curl -X DELETE -vu scmadmin:scmadmin http://localhost:8081/scm/api/v2/users/xyz
```

### Groups

#### GET

##### Collections 

```bash
curl -vu scmadmin:scmadmin "http://localhost:8081/scm/api/v2/groups/?sortBy=name&desc=true"
```

##### Individual

```bash
curl -vu scmadmin:scmadmin http://localhost:8081/scm/api/v2/groups/firstGroup
```

#### POST

```bash
curl -vu scmadmin:scmadmin --data '{                                                     
  "creationDate": "2018-06-28T07:42:45.281Z",
  "lastModified": "2018-06-28T07:42:45.281Z",
  "description": "descr",
  "name": "firstGroup",
  "type": "admin",
  "members": [ "scmadmin" ],
  "properties": {
    "pro1": "123",
        "pro2": "abc"
  },
  "links": {
    "empty": true
  }
 }' \
  --header "Content-Type: application/vnd.scmm-group+json" http://localhost:8081/scm/api/v2/groups/
```

#### PUT

```bash
curl -X PUT -vu scmadmin:scmadmin --data '{                                              
  "creationDate": "2018-06-28T07:42:45.281Z",
  "lastModified": "2018-06-28T07:42:45.281Z",
  "description": "descr",
  "name": "firstGroup",
  "type": "admin",
  "members": [ "scmadmin" ],
  "properties": {
    "pro1": "123",
        "pro2": "abc"
  },
  "links": {
    "empty": true
  }
 }' \
  --header "Content-Type: application/vnd.scmm-group+json" http://localhost:8081/scm/api/v2/groups/firstGroup
```

#### DELETE

```bash
curl -X DELETE -vu scmadmin:scmadmin http://localhost:8081/scm/api/v2/groups/firstGroup
```

### Repositories

#### GET

##### Collections 

```bash
curl -vu scmadmin:scmadmin "http://localhost:8081/scm/api/v2/repositories/?sortBy=name&pageSize=1&desc=true"
```

##### Individual

```bash
curl -vu scmadmin:scmadmin "http://localhost:8081/scm/api/v2/repositories/42/arepo"
```

#### POST

```bash
curl -vu scmadmin:scmadmin --data '{
  "contact": "a@con.tact",
  "creationDate": "2018-07-11T08:54:44.569Z",
  "description": "Desc",
  "name": "arepo",
  "type": "git"
 }' --header "Content-Type: application/vnd.scmm-repository+json" http://localhost:8081/scm/api/v2/repositories
```

#### PUT

- Change unmodifiable fields
  - type? -> Leads to 500 right now

```bash
curl -X PUT -vu scmadmin:scmadmin --data '{
  "contact": "anoter@con.tact",
  "creationDate": "2017-04-11T08:54:45.569Z",
  "description": "NEW", 
  "namespace": "42",
  "name": "arepo",
  "type": "git",
  "archived": "true"
 }' --header "Content-Type: application/vnd.scmm-repository+json" http://localhost:8081/scm/api/v2/repositories/42/arepo
```

#### DELETE

```bash
curl -X DELETE -vu scmadmin:scmadmin "http://localhost:8081/scm/api/v2/repositories/42/anSVNRepo"
```

### Repository Permissions

In this test we do not only test the REST endpoints themselves, but also the effect of the different permissions.

#### Prerequisites

For these tests we assume that you have created

- a git repository `scmadmin/git`, and
- a user named `user`.

If your entities have other ids, change them according to your data.

#### GET

This request should return an empty list of permissions:

```bash
curl -vu scmadmin:scmadmin "http://localhost:8081/scm/api/v2/repositories/scmadmin/git/permissions/"
```

#### POST / READ permission

```bash
curl -X POST -vu scmadmin:scmadmin --data '{
  "name": "user", "type":"READ"
  }' --header "Content-Type: application/vnd.scmm-permission+json"
  "http://localhost:8081/scm/api/v2/repositories/scmadmin/git/permissions/"
```

After this, you should be able to `GET` the repository with the user `user`:

```bash
curl -vu user:user "http://localhost:8081/scm/api/v2/repositories/scmadmin/git/permissions/"
```

Trying to change the repository using `PUT` with the user `user` should result in `403`:

```bash
curl -vu user:user -X PUT --data '{
  "contact": "zaphod.beeblebrox@hitchhiker.com",
  "namespace":"scmadmin",
  "name": "git",
  "archived": false,
  "type": "git"
}
' --header "Content-Type: application/vnd.scmm-repository+json" http://localhost:8081/scm/api/v2/repositories/scmadmin/git
```

Reading the permissions of the repository with the user `user` should result in `403`:

```bash
curl -vu user:user "http://localhost:8081/scm/api/v2/repositories/scmadmin/git/permissions/"
```

The user should be able to `clone` the repository:

```bash
git clone http://owner@localhost:8081/scm/git/scmadmin/git
```

The user should *not* be able to `push` to the repository:

```bash
cd git 
touch a
git add a
git commit -m a
git push
```

#### PUT / WRITE permission

It should be possible to change the permission for a specific user:

```bash
curl -X PUT -vu scmadmin:scmadmin --data '{
  "name": "user",
   "type":"WRITE"
   }' --header "Content-Type: application/vnd.scmm-permission+json" "http://localhost:8081/scm/api/v2/repositories/scmadmin/git/permissions/user"
```

After this the user `user` should now be able to `push` the repository created and modified beforehand.

```bash
cd git 
git push
```

#### OWNER permission

```bash
curl -X PUT -vu scmadmin:scmadmin --data '{
  "name": "user",
   "type":"OWNER"
   }' --header "Content-Type: application/vnd.scmm-permission+json" "http://localhost:8081/scm/api/v2/repositories/scmadmin/git/permissions/user"
```

After this, the user should be able to `GET` the permissions:

```bash
curl -vu user:user "http://localhost:8081/scm/api/v2/repositories/scmadmin/git/permissions/"
```

Additionally, the user should be able to change permissions:

```bash
curl -X PUT -vu scmadmin:scmadmin --data '{
  "name": "user",
   "type":"OWNER"
   }' --header "Content-Type: application/vnd.scmm-permission+json" "http://localhost:8081/scm/api/v2/repositories/scmadmin/git/permissions/user"
```

#### DELETE

Finally, a user with the role `OWNER` should be able to delete permissions:

```bash
curl -X DELETE -vu user:user "http://localhost:8081/scm/api/v2/repositories/scmadmin/git/permissions/user"
```

### Branches

* In advance: POST repo.
* Clone Repo, add Branches

#### GET

##### Collections 

```bash
curl -vu scmadmin:scmadmin "http://localhost:8081/scm/api/v2/repositories/scmadmin/arepo/branches"
```

##### Individual

```bash
curl -vu scmadmin:scmadmin "http://localhost:8081/scm/api/v2/repositories/scmadmin/arepo/branches/master"
```

### Configuration

#### GET

```bash
curl -vu scmadmin:scmadmin "http://localhost:8081/scm/api/v2/config"
```

#### PUT

```bash
curl -X PUT -vu scmadmin:scmadmin --data '{
  "proxyPassword": "pw",
  "proxyPort": 8082,
  "proxyServer": "proxy.mydomain.com",
  "proxyUser": "trillian",
  "enableProxy": false,
  "realmDescription": "SONIA :: SCM Manager",
  "enableRepositoryArchive": true,
  "disableGroupingGrid": true,
  "dateFormat": "YYYY-MM-DD HH:mm:ss",
  "anonymousAccessEnabled": false,
  "adminGroups": [ "admin", "plebs" ],
  "adminUsers": [ "trillian", "arthur" ],
  "baseUrl": "http://localhost:8081/scm",
  "forceBaseUrl": true,
  "loginAttemptLimit": 1,
  "proxyExcludes": [ "ex", "clude" ],
  "skipFailedAuthenticators": true,
  "pluginUrl": "url",
  "loginAttemptLimitTimeout": 0,
  "enabledXsrfProtection": false
 }' --header "Content-Type: application/vnd.scmm-config+json" http://localhost:8081/scm/api/v2/config
```

### Git Plugin Configuration

#### GET

```bash
curl -vu scmadmin:scmadmin "http://localhost:8081/scm/api/v2/config/git"
```

#### PUT

```bash
curl -X PUT -vu scmadmin:scmadmin --data '{
  "gcExpression": "0 0 14-6 ? * FRI-MON",
  "repositoryDirectory": "new",
  "disabled": true
 }' --header "Content-Type: application/vnd.scmm-gitConfig+json" http://localhost:8081/scm/api/v2/config/git
```

### Hg Plugin Configuration

#### GET

```bash
curl -vu scmadmin:scmadmin "http://localhost:8081/scm/api/v2/config/hg"
```

#### PUT

```bash
curl -X PUT -vu scmadmin:scmadmin --data '{
  "repositoryDirectory": "new",
  "disabled": true,
  "encoding": "UTF-16",
  "hgBinary": "/hg",
  "pythonBinary": "python3",
  "pythonPath": "gf",
  "useOptimizedBytecode": true,
  "showRevisionInId": true
 }' --header "Content-Type: application/vnd.scmm-hgConfig+json" http://localhost:8081/scm/api/v2/config/hg
```

#### Auto Config

##### Default

```bash
curl -v -X PUT -u scmadmin:scmadmin "http://localhost:8081/scm/api/v2/config/hg/auto-configuration"
```

##### Specific config

```bash
curl -v -X PUT -u scmadmin:scmadmin --data '{
  "repositoryDirectory": "new",
  "disabled": true,
  "encoding": "UTF-16",
  "hgBinary": "/hg",
  "pythonBinary": "python3",
  "pythonPath": "gf",
  "useOptimizedBytecode": true,
  "showRevisionInId": true
 }' --header "Content-Type: application/vnd.scmm-hgConfig+json" "http://localhost:8081/scm/api/v2/config/hg/auto-configuration"
```

#### Installations

##### Hg

```bash
curl -vu scmadmin:scmadmin "http://localhost:8081/scm/api/v2/config/hg/installations/hg" 
```

##### Python

```bash
curl -vu scmadmin:scmadmin "http://localhost:8081/scm/api/v2/config/hg/installations/python"
```

#### Packages

##### GET

```bash
curl -vu scmadmin:scmadmin "http://localhost:8081/scm/api/v2/config/hg/packages"
```

##### PUT

See [here](https://download.scm-manager.org/pkg/mercurial/packages.xml) for available packages. Will only work on Windows!

```bash
curl -X PUT -vu scmadmin:scmadmin "http://localhost:8081/scm/api/v2/config/hg/packages/4338c4_x64" 
```

### Svn Plugin Configuration

#### GET

```bash
curl -vu scmadmin:scmadmin "http://localhost:8081/scm/api/v2/config/svn"
```

#### PUT

```bash
curl -X PUT -vu scmadmin:scmadmin --data '{
  "repositoryDirectory": "new",
  "disabled": true,
  "enabledGZip": true,
  "compatibility": "PRE15"
 }' --header "Content-Type: application/vnd.scmm-svnConfig+json" http://localhost:8081/scm/api/v2/config/svn
```

### Repository Types

#### GET

#####  Collections 

```bash
curl -vu scmadmin:scmadmin "http://localhost:8081/scm/api/v2/repository-types"
```

##### Individual

```bash
curl -vu scmadmin:scmadmin "http://localhost:8081/scm/api/v2/repository-types/hg"
```

### Tags

#### GET

Pre-conditions: the git repository "HeartOfGold-git" exists and contains tags example v1.0 and v1.1

##### Collections 

```bash
curl -vu scmadmin:scmadmin "http://localhost:8081/scm/api/v2/repositories/scmadmin/HeartOfGold-git/tags/"
```

##### Individual

```bash
curl -vu scmadmin:scmadmin "http://localhost:8081/scm/api/v2/repositories/scmadmin/HeartOfGold-git/tags/v1.1"
```

### Content

#### git

##### Prepare

```bash
curl -vu scmadmin:scmadmin --data '{
  "contact": "a@con.tact",
  "creationDate": "2018-07-11T08:54:44.569Z",
  "description": "Desc",
  "name": "arepo",
  "type": "git"
 }' --header "Content-Type: application/vnd.scmm-repository+json" http://localhost:8081/scm/api/v2/repositories

cd /tmp
git clone http://scmadmin:scmadmin@localhost:8081/scm/git/scmadmin/arepo
cd arepo
echo "aaaa" > a
echo "bbb" > b.txt
wget https://bitbucket.org/sdorra/scm-manager/raw/f87655df229a94556aecf7d6b408ec0dcedb4e2a/scm-webapp/src/main/java/sonia/scm/api/RestActionResult.java
git add .
git commit -m 'Msg'
git push
```

##### Query and assert

```bash
# Assert Content type text plain
curl -X HEAD -vu scmadmin:scmadmin "http://localhost:8081/scm/api/v2/repositories/scmadmin/arepo/content/$(git rev-parse HEAD)/b.txt"  2>&1   | grep Content-Type
# Assert file content "bbb"
curl -vu scmadmin:scmadmin "http://localhost:8081/scm/api/v2/repositories/scmadmin/arepo/content/$(git rev-parse HEAD)/b.txt"

# Assert Content type octet stream
curl -X HEAD -vu scmadmin:scmadmin "http://localhost:8081/scm/api/v2/repositories/scmadmin/arepo/content/$(git rev-parse HEAD)/a" 2>&1 | grep Content-Type
# Assert file content "aaa"
curl -vu scmadmin:scmadmin "http://localhost:8081/scm/api/v2/repositories/scmadmin/arepo/content/$(git rev-parse HEAD)/a"

# Assert content type text/x-java-source & Language Header JAVA
curl -X HEAD -vu scmadmin:scmadmin "http://localhost:8081/scm/api/v2/repositories/scmadmin/arepo/content/$(git rev-parse HEAD)/RestActionResult.java" 2>&1 | grep -E 'Content-Type|Language'
# Assert java file content
curl -vu scmadmin:scmadmin "http://localhost:8081/scm/api/v2/repositories/scmadmin/arepo/content/$(git rev-parse HEAD)/RestActionResult.java"
```

#### hg

##### Prepare

```bash
curl -vu scmadmin:scmadmin --data '{
  "contact": "a@con.tact",
  "creationDate": "2018-07-11T08:54:44.569Z",
  "description": "Desc",
  "name": "hgrepo", 
  "type": "hg" 
 }' --header "Content-Type: application/vnd.scmm-repository+json" http://localhost:8081/scm/api/v2/repositories

hg clone http://scmadmin:scmadmin@localhost:8081/scm/hg/scmadmin/hgrepo
cd hgrepo
echo "aaaa" > a
echo "bbb" > b.txt
wget https://bitbucket.org/sdorra/scm-manager/raw/f87655df229a94556aecf7d6b408ec0dcedb4e2a/scm-webapp/src/main/java/sonia/scm/api/RestActionResult.java
hg add
hg commit -m 'msg'
hg push
```

##### Query and assert

```bash
# Assert Content type text plain
curl -X HEAD -vu scmadmin:scmadmin "http://localhost:8081/scm/api/v2/repositories/scmadmin/hgrepo/content/$(hg identify --id)/b.txt"  2>&1   | grep Content-Type
# Assert file content "bbb"
curl -vu scmadmin:scmadmin "http://localhost:8081/scm/api/v2/repositories/scmadmin/hgrepo/content/$(hg identify --id)/b.txt"

# Assert Content type octet stream
curl -X HEAD -vu scmadmin:scmadmin "http://localhost:8081/scm/api/v2/repositories/scmadmin/hgrepo/content/$(hg identify --id)/a" 2>&1 | grep Content-Type
# Assert file content "aaa"
curl -vu scmadmin:scmadmin "http://localhost:8081/scm/api/v2/repositories/scmadmin/hgrepo/content/$(hg identify --id)/a"

# Assert content type text/x-java-source & Language Header JAVA
curl -X HEAD -vu scmadmin:scmadmin "http://localhost:8081/scm/api/v2/repositories/scmadmin/hgrepo/content/$(hg identify --id)/RestActionResult.java" 2>&1 | grep -E 'Content-Type|Language'
# Assert java file content
curl -vu scmadmin:scmadmin "http://localhost:8081/scm/api/v2/repositories/scmadmin/hgrepo/content/$(hg identify --id)/RestActionResult.java"
```

#### svn

##### Prepare

```bash
curl -vu scmadmin:scmadmin --data '{
  "contact": "a@con.tact",
  "creationDate": "2018-07-11T08:54:44.569Z",
  "description": "Desc",
  "name": "svnrepo",
  "type": "svn"
 }' --header "Content-Type: application/vnd.scmm-repository+json" http://localhost:8081/scm/api/v2/repositories

svn co --non-interactive --no-auth-cache --username scmadmin --password scmadmin http://localhost:8081/scm/svn/scmadmin/svnrepo 
cd svnrepo
echo "aaaa" > a
echo "bbb" > b.txt
wget https://bitbucket.org/sdorra/scm-manager/raw/f87655df229a94556aecf7d6b408ec0dcedb4e2a/scm-webapp/src/main/java/sonia/scm/api/RestActionResult.java
svn add ./*
svn commit --non-interactive --no-auth-cache --username scmadmin --password scmadmin -m 'msg'
```

##### Query and assert

```bash
REVISION=$(svn --non-interactive --no-auth-cache --username scmadmin --password scmadmin info -r 'HEAD' --show-item revision | xargs echo -n)
# Assert Content type text plain
curl -X HEAD -vu scmadmin:scmadmin "http://localhost:8081/scm/api/v2/repositories/scmadmin/svnrepo/content/${REVISION}/b.txt"  2>&1   | grep Content-Type
# Assert file content "bbb"
curl -vu scmadmin:scmadmin "http://localhost:8081/scm/api/v2/repositories/scmadmin/svnrepo/content/${REVISION}/b.txt"

# Assert Content type octet stream
curl -X HEAD -vu scmadmin:scmadmin "http://localhost:8081/scm/api/v2/repositories/scmadmin/svnrepo/content/${REVISION}/a" 2>&1 | grep Content-Type
# Assert file content "aaa"
curl -vu scmadmin:scmadmin "http://localhost:8081/scm/api/v2/repositories/scmadmin/svnrepo/content/${REVISION}/a"

# Assert content type text/x-java-source & Language Header JAVA
curl -X HEAD -vu scmadmin:scmadmin "http://localhost:8081/scm/api/v2/repositories/scmadmin/svnrepo/content/${REVISION}/RestActionResult.java" 2>&1 | grep -E 'Content-Type|Language'
# Assert java file content
curl -vu scmadmin:scmadmin "http://localhost:8081/scm/api/v2/repositories/scmadmin/svnrepo/content/${REVISION}/RestActionResult.java"
```

### Access Token

#### Admin

##### Output all links of index resource

```bash
TOKEN=$(curl -s 'http://localhost:8081/scm/api/v2/auth/access_token' -H 'content-type: application/json' --data '{
  "cookie": false,
  "grant_type": "password",
  "username": "scmadmin",
  "password": "scmadmin"
}')
curl -s http://localhost:8081/scm/api/v2/ -H "Authorization: Bearer ${TOKEN}" | jq
```

##### Output only "config" and default logged in links 

default logged in links  = self, uiPlugins, me, logout

```bash
TOKEN=$(curl -s 'http://localhost:8081/scm/api/v2/auth/access_token' -H 'content-type: application/json' --data '{
  "cookie": false,
  "grant_type": "password",
  "username": "scmadmin",
  "password": "scmadmin",
  "scope": [
    "configuration:*"
  ]
}')
curl -s http://localhost:8081/scm/api/v2/ -H "Authorization: Bearer ${TOKEN}" | jq
```

#### non-Admin

Create non-admin user

```bash
curl -vu scmadmin:scmadmin --data '{
  "active": true,
  "admin": false,
  "displayName": "xyz",
  "mail": "x@abcde.cd",
  "name": "xyz",
  "password": "pwd123",
  "type": "xml"
  }' \
   --header "Content-Type: application/vnd.scmm-user+json;v=2"  http://localhost:8081/scm/api/v2/users/
```
   
##### Standard permissions of a logged in user without additional permissions

Standard links of a logged in user  = self, uiPlugins, me, logout, autocomplete, repositories

```bash
TOKEN=$(curl -s 'http://localhost:8081/scm/api/v2/auth/access_token' -H 'content-type: application/json' --data '{
  "cookie": false,
  "grant_type": "password",
  "username": "xyz",
  "password": "pwd123"
}')
curl -s http://localhost:8081/scm/api/v2/ -H "Authorization: Bearer ${TOKEN}" | jq
```

##### Scope requests permission the user doesn't have

This should not retrun `configuration` links, even though this scope was requested, because the user does not have the configuration permission. Otherwise this would be a major security flaw!
Compare to admin tests above.

```bash
TOKEN=$(curl -s 'http://localhost:8081/scm/api/v2/auth/access_token' -H 'content-type: application/json' --data '{
  "cookie": false,
  "grant_type": "password",
  "username": "xyz",
  "password": "pwd123",
  "scope": [
    "configuration:*"
  ]
}')
curl -s http://localhost:8081/scm/api/v2/ -H "Authorization: Bearer ${TOKEN}" | jq
```
