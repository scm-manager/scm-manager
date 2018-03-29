# Clone empty repository

GET /scm/hg/hgtest?cmd=capabilities HTTP/1.1.
Accept-Encoding: identity.
accept: application/mercurial-0.1.
authorization: Basic c2NtYWRtaW46c2NtYWRtaW4=.
host: localhost:8080.
user-agent: mercurial/proto-1.0 (Mercurial 4.3.1).

HTTP/1.1 200 OK.
Set-Cookie: JSESSIONID=1efk0qxy1dj5v133hev91zwsf4;Path=/scm.
Expires: Thu, 01 Jan 1970 00:00:00 GMT.
Set-Cookie: rememberMe=deleteMe; Path=/scm; Max-Age=0; Expires=Wed, 28-Mar-2018 05:57:18 GMT.
Content-Type: application/mercurial-0.1.
Content-Length: 130.
Server: Jetty(7.6.21.v20160908).
.
lookup changegroupsubset branchmap pushkey known getbundle unbundlehash batch stream unbundle=HG10GZ,HG10BZ,HG10UN httpheader=1024

GET /scm/hg/hgtest?cmd=listkeys HTTP/1.1.
Accept-Encoding: identity.
vary: X-HgArg-1.
x-hgarg-1: namespace=bookmarks.
accept: application/mercurial-0.1.
authorization: Basic c2NtYWRtaW46c2NtYWRtaW4=.
host: localhost:8080.
user-agent: mercurial/proto-1.0 (Mercurial 4.3.1).

HTTP/1.1 200 OK.
Set-Cookie: JSESSIONID=1rsxj8u1rq9wizawhyyxok2p5;Path=/scm.
Expires: Thu, 01 Jan 1970 00:00:00 GMT.
Set-Cookie: rememberMe=deleteMe; Path=/scm; Max-Age=0; Expires=Wed, 28-Mar-2018 05:57:18 GMT.
Content-Type: application/mercurial-0.1.
Content-Length: 0.
Server: Jetty(7.6.21.v20160908).

GET /scm/hg/hgtest?cmd=batch HTTP/1.1.
Accept-Encoding: identity.
vary: X-HgArg-1.
x-hgarg-1: cmds=heads+%3Bknown+nodes%3D.
accept: application/mercurial-0.1.
authorization: Basic c2NtYWRtaW46c2NtYWRtaW4=.
host: localhost:8080.
user-agent: mercurial/proto-1.0 (Mercurial 4.3.1).

HTTP/1.1 200 OK.
Set-Cookie: JSESSIONID=ewyx4m53d8dajjsob6gxobne;Path=/scm.
Expires: Thu, 01 Jan 1970 00:00:00 GMT.
Set-Cookie: rememberMe=deleteMe; Path=/scm; Max-Age=0; Expires=Wed, 28-Mar-2018 05:57:18 GMT.
Content-Type: application/mercurial-0.1.
Content-Length: 42.
Server: Jetty(7.6.21.v20160908).

0000000000000000000000000000000000000000
;

GET /scm/hg/hgtest?cmd=listkeys HTTP/1.1.
Accept-Encoding: identity.
vary: X-HgArg-1.
x-hgarg-1: namespace=phases.
accept: application/mercurial-0.1.
authorization: Basic c2NtYWRtaW46c2NtYWRtaW4=.
host: localhost:8080.
user-agent: mercurial/proto-1.0 (Mercurial 4.3.1).

HTTP/1.1 200 OK.
Set-Cookie: JSESSIONID=1o0hou15jtiywsywutf30qwm8;Path=/scm.
Expires: Thu, 01 Jan 1970 00:00:00 GMT.
Set-Cookie: rememberMe=deleteMe; Path=/scm; Max-Age=0; Expires=Wed, 28-Mar-2018 05:57:18 GMT.
Content-Type: application/mercurial-0.1.
Content-Length: 15.
Server: Jetty(7.6.21.v20160908).
.
publishing.True
