# Push multiple branches

GET /scm/hg/hgtest?cmd=capabilities HTTP/1.1.
Accept-Encoding: identity.
accept: application/mercurial-0.1.
authorization: Basic c2NtYWRtaW46c2NtYWRtaW4=.
host: localhost:8080.
user-agent: mercurial/proto-1.0 (Mercurial 4.3.1).

HTTP/1.1 200 OK.
Set-Cookie: JSESSIONID=1mvm1rxg8333iib7754ksusxc;Path=/scm.
Expires: Thu, 01 Jan 1970 00:00:00 GMT.
Set-Cookie: rememberMe=deleteMe; Path=/scm; Max-Age=0; Expires=Wed, 28-Mar-2018 06:16:50 GMT.
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

HTTP/1.1 200 OK.
Set-Cookie: JSESSIONID=58p9y9vcnz5cjs22dtw8mpwk;Path=/scm.
Expires: Thu, 01 Jan 1970 00:00:00 GMT.
Set-Cookie: rememberMe=deleteMe; Path=/scm; Max-Age=0; Expires=Wed, 28-Mar-2018 06:16:50 GMT.
Content-Type: application/mercurial-0.1.
Content-Length: 43.
Server: Jetty(7.6.21.v20160908).

c0ceccb3b2f0f5c977ff32b9337519e5f37942c2
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
Set-Cookie: JSESSIONID=v5wfwj8k4t261dp6808cdouoa;Path=/scm.
Expires: Thu, 01 Jan 1970 00:00:00 GMT.
Set-Cookie: rememberMe=deleteMe; Path=/scm; Max-Age=0; Expires=Wed, 28-Mar-2018 06:16:50 GMT.
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
Set-Cookie: JSESSIONID=3pgqytfhm4za1dco9p41j9yz5;Path=/scm.
Expires: Thu, 01 Jan 1970 00:00:00 GMT.
Set-Cookie: rememberMe=deleteMe; Path=/scm; Max-Age=0; Expires=Wed, 28-Mar-2018 06:16:50 GMT.
Content-Type: application/mercurial-0.1.
Content-Length: 0.
Server: Jetty(7.6.21.v20160908).

GET /scm/hg/hgtest?cmd=branchmap HTTP/1.1.
Accept-Encoding: identity.
accept: application/mercurial-0.1.
authorization: Basic c2NtYWRtaW46c2NtYWRtaW4=.
host: localhost:8080.
user-agent: mercurial/proto-1.0 (Mercurial 4.3.1).
.

HTTP/1.1 200 OK.
Set-Cookie: JSESSIONID=1tiz6zf7ui54e1j3d4vouxig5m;Path=/scm.
Expires: Thu, 01 Jan 1970 00:00:00 GMT.
Set-Cookie: rememberMe=deleteMe; Path=/scm; Max-Age=0; Expires=Wed, 28-Mar-2018 06:16:50 GMT.
Content-Type: application/mercurial-0.1.
Content-Length: 48.
Server: Jetty(7.6.21.v20160908).

default c0ceccb3b2f0f5c977ff32b9337519e5f37942c2

GET /scm/hg/hgtest?cmd=listkeys HTTP/1.1.
Accept-Encoding: identity.
vary: X-HgArg-1.
x-hgarg-1: namespace=bookmarks.
accept: application/mercurial-0.1.
authorization: Basic c2NtYWRtaW46c2NtYWRtaW4=.
host: localhost:8080.
user-agent: mercurial/proto-1.0 (Mercurial 4.3.1).

HTTP/1.1 200 OK.
Set-Cookie: JSESSIONID=1augu4tc71xax1dit20dtxzkez;Path=/scm.
Expires: Thu, 01 Jan 1970 00:00:00 GMT.
Set-Cookie: rememberMe=deleteMe; Path=/scm; Max-Age=0; Expires=Wed, 28-Mar-2018 06:16:50 GMT.
Content-Type: application/mercurial-0.1.
Content-Length: 0.
Server: Jetty(7.6.21.v20160908).

POST /scm/hg/hgtest?cmd=unbundle HTTP/1.1.
Accept-Encoding: identity.
content-type: application/mercurial-0.1.
vary: X-HgArg-1.
x-hgarg-1: heads=686173686564+95373ca7cd5371cb6c49bb755ee451d9ec585845.
accept: application/mercurial-0.1.
authorization: Basic c2NtYWRtaW46c2NtYWRtaW4=.
content-length: 746.
host: localhost:8080.
user-agent: mercurial/proto-1.0 (Mercurial 4.3.1).

HG10GZx...]H.Q...z..r.,.Y..Bw.~..c.Z&...hf.:......e.XK.X,...
,2.E1.B+...(.B"."*..z1.*......M...........93..k|..I..<...h..J_.L.9>.h..@.....op..^.....#....;.*..W....T@....!..dY....jT..A0O6.}..S.2..JPU.O6...aa...rY.VOf9.....7Ukj.&..<...z...j......%}..Jc.8c....k.."9.&".I.P.\..$.At......0..1..g.2.)<..$.. E..dn#....#.Y$3...n...5....J.e.......SNHN.q.MD..4..."I..`PF..?GH1..F..uES..Rl$47.....a........D.1...87.k.t..D..O_.3..6'cN.w.M..|@E.).X!.h*....U.B.X.....h..$.`4...
-..O.:./..oWN.....3...x.L......_[..../..k.R$.x.2..kkv.\2R....4...@.2...1Q..T
..(..m....s.Uo.......{.d.....Y....TYO...S.Pl`a5. ."N$.@...b...qJ.l.).n...1..F.Zy.....&>v;.q.....Jy..X.?.;....>U..|.....d.Y.*.q...NR.3...h.T..x..,.]...p{.^S.S...~..`..q.\j{.oCI.............K.....l9n.s......

HTTP/1.1 200 OK.
Set-Cookie: JSESSIONID=1e4fnqpncil9z1f7a2pya26nt7;Path=/scm.
Expires: Thu, 01 Jan 1970 00:00:00 GMT.
Set-Cookie: rememberMe=deleteMe; Path=/scm; Max-Age=0; Expires=Wed, 28-Mar-2018 06:16:50 GMT.
Content-Type: application/mercurial-0.1.
Content-Length: 102.
Server: Jetty(7.6.21.v20160908).

1
adding changesets
adding manifests
adding file changes
added 4 changesets with 2 changes to 2 files

GET /scm/hg/hgtest?cmd=listkeys HTTP/1.1.
Accept-Encoding: identity.
vary: X-HgArg-1.
x-hgarg-1: namespace=phases.
accept: application/mercurial-0.1.
authorization: Basic c2NtYWRtaW46c2NtYWRtaW4=.
host: localhost:8080.
user-agent: mercurial/proto-1.0 (Mercurial 4.3.1).

HTTP/1.1 200 OK.
Set-Cookie: JSESSIONID=f9hvrjssniym1qe33q0u8r2m8;Path=/scm.
Expires: Thu, 01 Jan 1970 00:00:00 GMT.
Set-Cookie: rememberMe=deleteMe; Path=/scm; Max-Age=0; Expires=Wed, 28-Mar-2018 06:16:50 GMT.
Content-Type: application/mercurial-0.1.
Content-Length: 101.
Server: Jetty(7.6.21.v20160908).

b5914611f84eae14543684b2721eec88b0edac12.1
187ddf37e237c370514487a0bb1a226f11a780b3.1
publishing.True

POST /scm/hg/hgtest?cmd=pushkey HTTP/1.1.
Accept-Encoding: identity.
content-type: application/mercurial-0.1.
vary: X-HgArg-1.
x-hgarg-1: key=ef5993bb4abb32a0565c347844c6d939fc4f4b98&namespace=phases&new=0&old=1.
accept: application/mercurial-0.1.
authorization: Basic c2NtYWRtaW46c2NtYWRtaW4=.
content-length: 0.
host: localhost:8080.
user-agent: mercurial/proto-1.0 (Mercurial 4.3.1).

HTTP/1.1 200 OK.
Set-Cookie: JSESSIONID=z5lrut6940a650sw6x9bls8a;Path=/scm.
Expires: Thu, 01 Jan 1970 00:00:00 GMT.
Set-Cookie: rememberMe=deleteMe; Path=/scm; Max-Age=0; Expires=Wed, 28-Mar-2018 06:16:50 GMT.
Content-Type: application/mercurial-0.1.
Content-Length: 2.
Server: Jetty(7.6.21.v20160908).

1
