# Push single changeset

```http
GET /scm/hg/hgtest?cmd=capabilities HTTP/1.1.
Accept-Encoding: identity.
accept: application/mercurial-0.1.
authorization: Basic c2NtYWRtaW46c2NtYWRtaW4=.
host: localhost:8080.
user-agent: mercurial/proto-1.0 (Mercurial 4.3.1).

HTTP/1.1 200 OK.
Set-Cookie: JSESSIONID=18r2i2jsba46d14ncsmcjdhaem;Path=/scm.
Expires: Thu, 01 Jan 1970 00:00:00 GMT.
Set-Cookie: rememberMe=deleteMe; Path=/scm; Max-Age=0; Expires=Wed, 28-Mar-2018 06:03:35 GMT.
Content-Type: application/mercurial-0.1.
Content-Length: 130.
Server: Jetty(7.6.21.v20160908).

lookup changegroupsubset branchmap pushkey known getbundle unbundlehash batch stream unbundle=HG10GZ,HG10BZ,HG10UN httpheader=1024

GET /scm/hg/hgtest?cmd=batch HTTP/1.1.
Accept-Encoding: identity.
vary: X-HgArg-1.
x-hgarg-1: cmds=heads+%3Bknown+nodes%3Dc0ceccb3b2f0f5c977ff32b9337519e5f37942c2.
accept: application/mercurial-0.1.
authorization: Basic c2NtYWRtaW46c2NtYWRtaW4=.
host: localhost:8080.
user-agent: mercurial/proto-1.0 (Mercurial 4.3.1).

HTTP/1.1 200 OK.
Set-Cookie: JSESSIONID=1fw0i0c5zpy281gfgha0f26git;Path=/scm.
Expires: Thu, 01 Jan 1970 00:00:00 GMT.
Set-Cookie: rememberMe=deleteMe; Path=/scm; Max-Age=0; Expires=Wed, 28-Mar-2018 06:03:35 GMT.
Content-Type: application/mercurial-0.1.
Content-Length: 43.
Server: Jetty(7.6.21.v20160908).

0000000000000000000000000000000000000000
;0

GET /scm/hg/hgtest?cmd=listkeys HTTP/1.1.
Accept-Encoding: identity.
vary: X-HgArg-1.
x-hgarg-1: namespace=phases.
accept: application/mercurial-0.1.
authorization: Basic c2NtYWRtaW46c2NtYWRtaW4=.
host: localhost:8080.
user-agent: mercurial/proto-1.0 (Mercurial 4.3.1).

HTTP/1.1 200 OK.
Set-Cookie: JSESSIONID=dfa46uaqgf39w3jhk857oymu;Path=/scm.
Expires: Thu, 01 Jan 1970 00:00:00 GMT.
Set-Cookie: rememberMe=deleteMe; Path=/scm; Max-Age=0; Expires=Wed, 28-Mar-2018 06:03:35 GMT.
Content-Type: application/mercurial-0.1.
Content-Length: 15.
Server: Jetty(7.6.21.v20160908).

publishing.True

GET /scm/hg/hgtest?cmd=listkeys HTTP/1.1.
Accept-Encoding: identity.
vary: X-HgArg-1.
x-hgarg-1: namespace=bookmarks.
accept: application/mercurial-0.1.
authorization: Basic c2NtYWRtaW46c2NtYWRtaW4=.
host: localhost:8080.
user-agent: mercurial/proto-1.0 (Mercurial 4.3.1).

HTTP/1.1 200 OK.
Set-Cookie: JSESSIONID=2sk1llvrsagg33xgmwyirfpi;Path=/scm.
Expires: Thu, 01 Jan 1970 00:00:00 GMT.
Set-Cookie: rememberMe=deleteMe; Path=/scm; Max-Age=0; Expires=Wed, 28-Mar-2018 06:03:35 GMT.
Content-Type: application/mercurial-0.1.
Content-Length: 0.
Server: Jetty(7.6.21.v20160908).

POST /scm/hg/hgtest?cmd=unbundle HTTP/1.1.
Accept-Encoding: identity.
content-type: application/mercurial-0.1.
vary: X-HgArg-1.
x-hgarg-1: heads=686173686564+6768033e216468247bd031a0a2d9876d79818f8f.
accept: application/mercurial-0.1.
authorization: Basic c2NtYWRtaW46c2NtYWRtaW4=.
content-length: 261.
host: localhost:8080.
user-agent: mercurial/proto-1.0 (Mercurial 4.3.1).

HG10GZx.c``8w.....>|=Y..h.q.....N.......%......Z....&&&.&...YZ.&.&[$.........$.%q..&%..d&.).....%*.....Y.....9z...v\..FF......
..F..\.z%.%\\.)).)
.P[....D..[un..L).nc..q.m*.H.l#C...eZJ..YJ.Q.qR...e.aJ.EjjJ.AZ..A.Q..E.1.T.'D..C....7s.}..4G........3.S.mL.0.....zk

HTTP/1.1 200 OK.
Set-Cookie: JSESSIONID=hlucs5utn1ifnpehqmjpt593;Path=/scm.
Expires: Thu, 01 Jan 1970 00:00:00 GMT.
Set-Cookie: rememberMe=deleteMe; Path=/scm; Max-Age=0; Expires=Wed, 28-Mar-2018 06:03:35 GMT.
Content-Type: application/mercurial-0.1.
Content-Length: 102.
Server: Jetty(7.6.21.v20160908).

1
adding changesets
adding manifests
adding file changes
added 1 changesets with 1 changes to 1 files

T 172.17.0.1:33206 -> 172.17.0.2:8080 [AP]
GET /scm/hg/hgtest?cmd=listkeys HTTP/1.1.
Accept-Encoding: identity.
vary: X-HgArg-1.
x-hgarg-1: namespace=phases.
accept: application/mercurial-0.1.
authorization: Basic c2NtYWRtaW46c2NtYWRtaW4=.
host: localhost:8080.
user-agent: mercurial/proto-1.0 (Mercurial 4.3.1).

HTTP/1.1 200 OK.
Set-Cookie: JSESSIONID=15xomlrxl8qja1cj47rjpqda0y;Path=/scm.
Expires: Thu, 01 Jan 1970 00:00:00 GMT.
Set-Cookie: rememberMe=deleteMe; Path=/scm; Max-Age=0; Expires=Wed, 28-Mar-2018 06:03:35 GMT.
Content-Type: application/mercurial-0.1.
Content-Length: 58.
Server: Jetty(7.6.21.v20160908).

c0ceccb3b2f0f5c977ff32b9337519e5f37942c2.1
publishing.True

POST /scm/hg/hgtest?cmd=pushkey HTTP/1.1.
Accept-Encoding: identity.
content-type: application/mercurial-0.1.
vary: X-HgArg-1.
x-hgarg-1: key=c0ceccb3b2f0f5c977ff32b9337519e5f37942c2&namespace=phases&new=0&old=1.
accept: application/mercurial-0.1.
authorization: Basic c2NtYWRtaW46c2NtYWRtaW4=.
content-length: 0.
host: localhost:8080.
user-agent: mercurial/proto-1.0 (Mercurial 4.3.1).

HTTP/1.1 200 OK.
Set-Cookie: JSESSIONID=5zrop5v8e661ipk12tvru525;Path=/scm.
Expires: Thu, 01 Jan 1970 00:00:00 GMT.
Set-Cookie: rememberMe=deleteMe; Path=/scm; Max-Age=0; Expires=Wed, 28-Mar-2018 06:03:35 GMT.
Content-Type: application/mercurial-0.1.
Content-Length: 2.
Server: Jetty(7.6.21.v20160908).

1
```
