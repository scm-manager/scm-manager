---
title: Reverse Proxy
subtitle: How to use SCM-Manager with common reverse proxies
displayToc: true
---

SCM-Manager can run behind any reverse proxy, but a few rules must be respected.
The reverse proxy should not encode slashes and the `X-Forwarded-For` and `X-Forwarded-Host` headers must be sent to
SCM-Manager.
If the proxy uses a different protocol as the SCM-Manager e.g. https on proxy and http on scm-manager, the
`X-Forwarded-Proto` header must be sent too.
If `XSRF protection` is enabled on the SCM-Manager server, the cookie has to be `HttpOnly=false` and must not be
modified.

For SCM-Manager to work properly, the configuration `forwardHeadersEnabled` has to be set to `true` in the `config.yml`.
To avoid timeouts due to caching in the reverse proxies, you also might want to increase the `idleTimeout` to a higher
value, depending on the size of your repositories (you might want to start with `300000`, that would be five minutes).
See the section about reverse proxies in [SCM-Server configuration](../scm-server/) for more information.

## nginx

```nginx
# set required forward headers
proxy_set_header X-Forwarded-Host $host:$server_port;
proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
# if https is used make sure X-Forwarded-Proto header is send
proxy_set_header X-Forwarded-Proto $scheme;

# assuming scm-manager is running on localhost at port 8080
location /scm {
    proxy_pass http://scm:8080;
}
```

## Apache

If you use `VirtualHost` sections, please make sure to put the directives `AllowEncodedSlashes`, `RequestHeader`,
`ProxyPass`, and `ProxyPassReverse` into the same section as the `Location` for SCM-Manager.

```apacheconf
# Ensure mod_proxy and mod_proxy_http modules are loaded
LoadModule proxy_module modules/mod_proxy.so
LoadModule proxy_http_module modules/mod_proxy_http.so

# avoid encoding of slashes
AllowEncodedSlashes NoDecode

# if https is used, make sure X-Forwarded-Proto is send
RequestHeader set "X-Forwarded-Proto" expr=%{REQUEST_SCHEME}
RequestHeader set "X-Forwarded-SSL" expr=%{HTTPS}

# assuming scm-manager is running on localhost at port 8080
ProxyPass /scm http://localhost:8080/scm nocanon
ProxyPassReverse /scm http://localhost:8080/scm

<Location /scm>
    Order allow,deny
    Allow from all
</Location>
```

### Notes

* Setting ProxyPassReverseCookiePath would most likely cause problems with session handling!
* If you encounter timeout problems, please have a look at [Apache Module mod_proxy#Workers](http://httpd.apache.org/docs/current/mod/mod_proxy.html#workers).

## HAProxy

```apacheconf
backend scm
    # use http as proxy protocol
    mode http
    # sets X-Forwarded-For header
    option forwardfor
    # check if scm is running
    option httpchk GET /scm/api/v2
    # assuming scm-manager is running on localhost at port 8080
    server dcscm1 localhost:8080 check
    # sets X-Forwarded-Host header
    http-request set-header X-Forwarded-Host %[req.hdr(Host)]
    # sets X-Forwarded-Proto to https if ssl is enabled
    http-request set-header X-Forwarded-Proto https if { ssl_fc }
```
