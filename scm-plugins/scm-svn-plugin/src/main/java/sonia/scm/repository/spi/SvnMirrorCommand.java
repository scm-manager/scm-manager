/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.repository.spi;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.auth.SVNAuthentication;
import org.tmatesoft.svn.core.auth.SVNPasswordAuthentication;
import org.tmatesoft.svn.core.auth.SVNSSLAuthentication;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.wc.admin.SVNAdminClient;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.api.MirrorCommandResult;
import sonia.scm.repository.api.Pkcs12ClientCertificateCredential;
import sonia.scm.repository.api.UsernamePasswordCredential;

import javax.net.ssl.TrustManager;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

import static sonia.scm.ContextEntry.ContextBuilder.entity;

public class SvnMirrorCommand extends AbstractSvnCommand implements MirrorCommand {

  private final TrustManager trustManager;
  private final Function<File, SVNURL> urlFactory;

  SvnMirrorCommand(SvnContext context, TrustManager trustManager, Function<File, SVNURL> urlFactory) {
    super(context);
    this.trustManager = trustManager;
    this.urlFactory = urlFactory;
  }

  @Override
  public MirrorCommandResult mirror(MirrorCommandRequest mirrorCommandRequest) {

    try {
      SVNURL url = urlFactory.apply(context.getDirectory());

      SVNURL next = SVNURL.parseURIEncoded(mirrorCommandRequest.getSourceUrl());

      SVNAdminClient admin = createAdminClient(url, mirrorCommandRequest);

      admin.doCompleteSynchronize(next, url);
    } catch (SVNException e) {
      throw new InternalRepositoryException(entity(String.class, mirrorCommandRequest.getSourceUrl()), "could not mirror svn repository", e);
    }
    return null;
  }

  private SVNAdminClient createAdminClient(SVNURL url, MirrorCommandRequest mirrorCommandRequest) {
    Collection<SVNAuthentication> authentications = new ArrayList<>();
    mirrorCommandRequest.getCredential(Pkcs12ClientCertificateCredential.class)
      .map(c -> createTlsAuth(url, c))
      .ifPresent(authentications::add);
    mirrorCommandRequest.getCredential(UsernamePasswordCredential.class)
      .map(c -> SVNPasswordAuthentication.newInstance(c.username(), c.password(), false, url, false))
      .ifPresent(authentications::add);
    ISVNAuthenticationManager authManager = new BasicAuthenticationManager(
      authentications.toArray(new SVNAuthentication[authentications.size()])) {
      @Override
      public TrustManager getTrustManager(SVNURL url) {
        return trustManager;
      }
    };

    return new SVNAdminClient(authManager, SVNWCUtil.createDefaultOptions(true));
  }

  private SVNSSLAuthentication createTlsAuth(SVNURL url, Pkcs12ClientCertificateCredential c) {
    return SVNSSLAuthentication.newInstance(
      c.getCertificate().toFile(),
      c.getPassword(),
      false,
      url,
      true);
  }

  @Override
  public MirrorCommandResult update(MirrorCommandRequest mirrorCommandRequest) {
    return null;
  }
}
