# Push bookmark

```http
GET /scm/hg/hgtest?cmd=capabilities HTTP/1.1.
Accept-Encoding: identity.
accept: application/mercurial-0.1.
authorization: Basic c2NtYWRtaW46c2NtYWRtaW4=.
host: localhost:8080.
user-agent: mercurial/proto-1.0 (Mercurial 4.3.1).

HTTP/1.1 200 OK.
Set-Cookie: JSESSIONID=7rq9vpp9svfm1sicq7h9vetmv;Path=/scm.
Expires: Thu, 01 Jan 1970 00:00:00 GMT.
Set-Cookie: rememberMe=deleteMe; Path=/scm; Max-Age=0; Expires=Wed, 28-Mar-2018 08:08:35 GMT.
Content-Type: application/mercurial-0.1.
Content-Length: 130.
Server: Jetty(7.6.21.v20160908).

lookup changegroupsubset branchmap pushkey known getbundle unbundlehash batch stream unbundle=HG10GZ,HG10BZ,HG10UN httpheader=1024

GET /scm/hg/hgtest?cmd=batch HTTP/1.1.
Accept-Encoding: identity.
vary: X-HgArg-1.
x-hgarg-1: cmds=heads+%3Bknown+nodes%3Def5993bb4abb32a0565c347844c6d939fc4f4b98.
accept: application/mercurial-0.1.
authorization: Basic c2NtYWRtaW46c2NtYWRtaW4=.
host: localhost:8080.
user-agent: mercurial/proto-1.0 (Mercurial 4.3.1).

T 172.17.0.2:8080 -> 172.17.0.1:36576 [AP]
HTTP/1.1 200 OK.
Set-Cookie: JSESSIONID=1553csz4sf7scyvw8mqnqfirn;Path=/scm.
Expires: Thu, 01 Jan 1970 00:00:00 GMT.
Set-Cookie: rememberMe=deleteMe; Path=/scm; Max-Age=0; Expires=Wed, 28-Mar-2018 08:08:35 GMT.
Content-Type: application/mercurial-0.1.
Content-Length: 43.
Server: Jetty(7.6.21.v20160908).

ef5993bb4abb32a0565c347844c6d939fc4f4b98
;1

GET /scm/hg/hgtest?cmd=listkeys HTTP/1.1.
Accept-Encoding: identity.
vary: X-HgArg-1.
x-hgarg-1: namespace=phases.
accept: application/mercurial-0.1.
authorization: Basic c2NtYWRtaW46c2NtYWRtaW4=.
host: localhost:8080.
user-agent: mercurial/proto-1.0 (Mercurial 4.3.1).

HTTP/1.1 200 OK.
Set-Cookie: JSESSIONID=11xa5u3nrmx8k1nar3sazg6jzh;Path=/scm.
Expires: Thu, 01 Jan 1970 00:00:00 GMT.
Set-Cookie: rememberMe=deleteMe; Path=/scm; Max-Age=0; Expires=Wed, 28-Mar-2018 08:08:35 GMT.
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
Set-Cookie: JSESSIONID=1p1uzcvfe1pvzh2buzo658rxw;Path=/scm.
Expires: Thu, 01 Jan 1970 00:00:00 GMT.
Set-Cookie: rememberMe=deleteMe; Path=/scm; Max-Age=0; Expires=Wed, 28-Mar-2018 08:08:35 GMT.
Content-Type: application/mercurial-0.1.
Content-Length: 0.
Server: Jetty(7.6.21.v20160908).

GET /scm/hg/hgtest?cmd=listkeys HTTP/1.1.
Accept-Encoding: identity.
vary: X-HgArg-1.
x-hgarg-1: namespace=phases.
accept: application/mercurial-0.1.
authorization: Basic c2NtYWRtaW46c2NtYWRtaW4=.
host: localhost:8080.
user-agent: mercurial/proto-1.0 (Mercurial 4.3.1).

HTTP/1.1 200 OK.
Set-Cookie: JSESSIONID=1mhlj3ucfzdp6ifmzoua4zwit;Path=/scm.
Expires: Thu, 01 Jan 1970 00:00:00 GMT.
Set-Cookie: rememberMe=deleteMe; Path=/scm; Max-Age=0; Expires=Wed, 28-Mar-2018 08:08:35 GMT.
Content-Type: application/mercurial-0.1.
Content-Length: 15.
Server: Jetty(7.6.21.v20160908).

publishing.True

POST /scm/hg/hgtest?cmd=pushkey HTTP/1.1.
Accept-Encoding: identity.
content-type: application/mercurial-0.1.
vary: X-HgArg-1.
x-hgarg-1: key=markone&namespace=bookmarks&new=ef5993bb4abb32a0565c347844c6d939fc4f4b98&old=.
accept: application/mercurial-0.1.
authorization: Basic c2NtYWRtaW46c2NtYWRtaW4=.
content-length: 0.
host: localhost:8080.
user-agent: mercurial/proto-1.0 (Mercurial 4.3.1).

HTTP/1.1 200 OK.
Set-Cookie: JSESSIONID=s4vtagb303dv1xg809wnp7e8z;Path=/scm.
Expires: Thu, 01 Jan 1970 00:00:00 GMT.
Set-Cookie: rememberMe=deleteMe; Path=/scm; Max-Age=0; Expires=Wed, 28-Mar-2018 08:08:35 GMT.
Content-Type: application/mercurial-0.1.
Content-Length: 2.
Server: Jetty(7.6.21.v20160908).
.
1
```
