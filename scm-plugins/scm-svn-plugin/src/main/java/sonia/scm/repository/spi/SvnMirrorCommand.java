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

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.ArrayList;
import java.util.Collection;

import static java.util.Arrays.asList;
import static sonia.scm.repository.api.MirrorCommandResult.ResultType.FAILED;
import static sonia.scm.repository.api.MirrorCommandResult.ResultType.OK;

public class SvnMirrorCommand extends AbstractSvnCommand implements MirrorCommand {

  private static final Logger LOG = LoggerFactory.getLogger(SvnMirrorCommand.class);

  private final TrustManager trustManager;

  SvnMirrorCommand(SvnContext context, TrustManager trustManager) {
    super(context);
    this.trustManager = trustManager;
  }

  @Override
  public MirrorCommandResult mirror(MirrorCommandRequest mirrorCommandRequest) {
    SVNURL url = createUrlForLocalRepository();
    return withAdminClient(mirrorCommandRequest, admin -> {
      SVNURL source = SVNURL.parseURIEncoded(mirrorCommandRequest.getSourceUrl());
      admin.doCompleteSynchronize(source, url);
    });
  }

  @Override
  public MirrorCommandResult update(MirrorCommandRequest mirrorCommandRequest) {
    SVNURL url = createUrlForLocalRepository();
    return withAdminClient(mirrorCommandRequest, admin -> admin.doSynchronize(url));
  }

  private MirrorCommandResult withAdminClient(MirrorCommandRequest mirrorCommandRequest, AdminConsumer consumer) {
    Stopwatch stopwatch = Stopwatch.createStarted();
    long beforeUpdate;
    long afterUpdate;
    try {
      beforeUpdate = context.open().getLatestRevision();
      SVNURL url = createUrlForLocalRepository();
      SVNAdminClient admin = createAdminClient(url, mirrorCommandRequest);

      handleConsumer(mirrorCommandRequest, consumer, url, admin);
      afterUpdate = context.open().getLatestRevision();
    } catch (SVNException e) {
      LOG.info("Could not mirror svn repository", e);
      return new MirrorCommandResult(
        FAILED,
        asList(
          "failed to synchronize. See following error message for more details:",
          e.getMessage()
        ),
        stopwatch.stop().elapsed());
    }
    return new MirrorCommandResult(
      OK,
      ImmutableList.of("Updated from revision " + beforeUpdate + " to revision " + afterUpdate),
      stopwatch.stop().elapsed()
    );
  }

  private void handleConsumer(MirrorCommandRequest mirrorCommandRequest, AdminConsumer consumer, SVNURL url, SVNAdminClient admin) throws SVNException {
    try {
      consumer.accept(admin);
    } catch (SVNException e) {
      if (e.getMessage().equals("svn: E204899: Destination repository has not been initialized")) {
        SVNURL source = SVNURL.parseURIEncoded(mirrorCommandRequest.getSourceUrl());
        admin.doCompleteSynchronize(source, url);
      } else {
        throw e;
      }
    }
  }

  private SVNURL createUrlForLocalRepository() {
    try {
      return SVNURL.fromFile(context.getDirectory());
    } catch (SVNException e) {
      throw new InternalRepositoryException(repository, "could not create svn url for local repository", e);
    }
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
      c.getCertificate(),
      c.getPassword(),
      false,
      url,
      true);
  }

  private interface AdminConsumer {
    void accept(SVNAdminClient adminClient) throws SVNException;
  }
}
