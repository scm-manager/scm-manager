---
title: SCM-Server Configuration
subtitle: Various configuration options for the SCM-Server
displayToc: true
---

## Https

In order to use https with scm-server, you need a keystore with a certificate and the corresponding secret key.
In the following we will use `openssl` to create a self signed certificate for demonstration purposes. 

### Create self signed certificate

**Warning**: Do not use self signed certificates in production, this is only for demonstration purposes.

```bash
openssl req -new -x509 -newkey rsa:2048 -sha256 -keyout tls.key -out tls.crt
```

This command will ask a few questions about metadata for generated certificate:

* PEM pass phrase: This is a password to protect the scret key
* Country Name (2 letter code)
* State or Province Name (full name)
* Locality Name (eg, city)
* Organization Name (eg, company)
* Organizational Unit Name (eg, section)
* Common Name (eg, fully qualified host name)
* Email Address

Make sure that the common name matches the fqdn, which you are using to access SCM-Manager.

#### Browsers

In order to use a self signed certificate the certificate must be imported into you browser.

#### Configure Git

To use git with a self signed certificate, we have to add the certificate path to the configuration.

```bash
git config http.sslCAInfo /complete/path/to/tls.crt
```

#### Configure Mercurial

To use mercurial with a self signed certificate, we have to add the certificate path to the configuration.

```ini
[web]
cacerts = /complete/path/to/cert.pem
```

### Create keystore

Create a keystore in pkcs12 format.
This command can be used with the self signed certificate from above or with a valid certificate from an authority.

```bash
openssl pkcs12 -inkey tls.key -in tls.crt -export -out keystore.pkcs12
```

If your secret key is protected with a pass phrase, you have to enter it first.
Than you have to enter an export password to protect your keystore.

### Server configuration

Add the following snippet at the end of your `server-config.xml`, be sure it is inside the `Configure` tag:

```xml
<!-- ssl configuration start -->

<New id="sslContextFactory" class="org.eclipse.jetty.util.ssl.SslContextFactory$Server">
  <!-- 
    path to your keystore, it can be a java keystore or in the pkcs12 format
  -->
  <Set name="KeyStorePath">
    <SystemProperty name="basedir" default="."/>/conf/keystore.pkcs12
  </Set>
  <!--
    use pkcs12 or jks for java keystore
  -->
  <Set name="KeyStoreType">PKCS12</Set>
  <!--
    the password of you keystore
  -->
  <Set name="KeyStorePassword">secret</Set>

  <!--
    For a more up to date list of ciphers and protocols, have a look at the mozilla ssl configurator:
    https://ssl-config.mozilla.org/#server=jetty&version=9.4.28&config=intermediate&guideline=5.4
  -->

  <!-- TLS 1.3 requires Java 11 or higher -->
  <Set name="IncludeProtocols">
    <Array type="String">
        <Item>TLSv1.2</Item>
        <Item>TLSv1.3</Item>
    </Array>
  </Set>

  <Set name="IncludeCipherSuites">
    <Array type="String">
      <Item>TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384</Item>
      <Item>TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384</Item>
      <Item>TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256</Item>
      <Item>TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256</Item>
      <Item>TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256</Item>
      <Item>TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256</Item>
      <Item>TLS_DHE_RSA_WITH_AES_256_GCM_SHA384</Item>
      <Item>TLS_DHE_RSA_WITH_AES_128_GCM_SHA256</Item>
    </Array>
  </Set>

  <Set name="useCipherSuitesOrder">
    <Property name="jetty.sslContext.useCipherSuitesOrder" default="false" />
  </Set>
</New>

<New id="sslHttpConfig" class="org.eclipse.jetty.server.HttpConfiguration">
  <Arg>
    <Ref refid="httpConfig"/>
  </Arg>
  <Call name="addCustomizer">
    <Arg>
      <New class="org.eclipse.jetty.server.SecureRequestCustomizer">
        <Arg name="sniRequired" type="boolean"><Property name="jetty.ssl.sniRequired" default="false"/></Arg>
        <Arg name="sniHostCheck" type="boolean"><Property name="jetty.ssl.sniHostCheck" default="true"/></Arg>
        <Arg name="stsMaxAgeSeconds" type="int"><Property name="jetty.ssl.stsMaxAgeSeconds" default="-1"/></Arg>
        <Arg name="stsIncludeSubdomains" type="boolean"><Property name="jetty.ssl.stsIncludeSubdomains" default="false"/></Arg>
      </New>
    </Arg>
  </Call>
</New>

<Call name="addConnector">
  <Arg>
    <New id="sslConnector" class="org.eclipse.jetty.server.ServerConnector">
      <Arg name="server">
        <Ref refid="ScmServer" />
      </Arg>
      <Arg name="factories">
        <Array type="org.eclipse.jetty.server.ConnectionFactory">
          <Item>
            <New class="org.eclipse.jetty.server.SslConnectionFactory">
              <Arg name="next">http/1.1</Arg>
              <Arg name="sslContextFactory">
                <Ref refid="sslContextFactory"/>
              </Arg>
            </New>
          </Item>
          <Item>
            <New class="org.eclipse.jetty.server.HttpConnectionFactory">
              <Arg name="config">
                <Ref refid="sslHttpConfig" />
              </Arg>
            </New>
          </Item>
        </Array>
      </Arg>
      <!--
        Address to listen 0.0.0.0 means on every interface
      -->
      <Set name="host">
        <SystemProperty name="jetty.host" default="0.0.0.0" />
      </Set>
      <!--
        Port for the https connector
      -->
      <Set name="port">
        <Property name="jetty.ssl.port" default="8443" />
      </Set>
    </New>
  </Arg>
</Call>

<!-- ssl configuration end -->
```

The snipped above assumes your keystore is in the pkcs12 format and is stored at `conf/keystore.pkcs12` with the password `secret`.
You have to tweek this settings to match your setup.
After modifying your `server-config.xml`, you have to **restart** your SCM-Manager instance.
Now SCM-Manager should open a second port with **https** (in the example above **8443**).
