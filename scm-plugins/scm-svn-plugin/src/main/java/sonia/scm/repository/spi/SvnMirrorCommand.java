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
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.wc.admin.SVNAdminClient;
import sonia.scm.net.GlobalProxyConfiguration;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.api.MirrorCommandResult;

import javax.net.ssl.TrustManager;

import static java.util.Arrays.asList;
import static sonia.scm.repository.api.MirrorCommandResult.ResultType.FAILED;
import static sonia.scm.repository.api.MirrorCommandResult.ResultType.OK;

public class SvnMirrorCommand extends AbstractSvnCommand implements MirrorCommand {

  private static final Logger LOG = LoggerFactory.getLogger(SvnMirrorCommand.class);
  private static final int TARGET_NOT_INITIALIZED_ERROR_CODE = 204899;

  private final SvnMirrorAuthenticationFactory authenticationFactory;

  SvnMirrorCommand(SvnContext context, TrustManager trustManager, GlobalProxyConfiguration globalProxyConfiguration) {
    this(context, new SvnMirrorAuthenticationFactory(trustManager, globalProxyConfiguration));
  }

  SvnMirrorCommand(SvnContext context, SvnMirrorAuthenticationFactory authenticationFactory) {
    super(context);
    this.authenticationFactory = authenticationFactory;
  }

  @Override
  public MirrorCommandResult mirror(MirrorCommandRequest mirrorCommandRequest) {
    return mirror(mirrorCommandRequest, SVNAdminClient::doCompleteSynchronize);
  }

  @Override
  public MirrorCommandResult update(MirrorCommandRequest mirrorCommandRequest) {
    return mirror(mirrorCommandRequest, this::doUpdate);
  }

  private void doUpdate(SVNAdminClient admin, SVNURL sourceUrl, SVNURL targetUrl) throws SVNException {
    try {
      admin.doSynchronize(targetUrl);
    } catch (SVNException e) {
      if (isTargetNotInitializedException(e)) {
        LOG.warn("update failed, because destination repository has not been initialized, start complete synchronize");
        admin.doCompleteSynchronize(sourceUrl, targetUrl);
      } else {
        throw e;
      }
    }
  }

  private boolean isTargetNotInitializedException(SVNException e) {
    return e.getErrorMessage().getErrorCode().getCode() == TARGET_NOT_INITIALIZED_ERROR_CODE;
  }

  private MirrorCommandResult mirror(MirrorCommandRequest mirrorCommandRequest, Worker worker) {
    Stopwatch stopwatch = Stopwatch.createStarted();
    long beforeUpdate;
    long afterUpdate;
    try {
      beforeUpdate = context.open().getLatestRevision();

      SVNURL sourceUrl = SVNURL.parseURIEncoded(mirrorCommandRequest.getSourceUrl());
      SVNURL targetUrl = createUrlForLocalRepository();
      SVNAdminClient admin = createAdminClient(sourceUrl, mirrorCommandRequest);

      worker.doWork(admin, sourceUrl, targetUrl);

      afterUpdate = context.open().getLatestRevision();
    } catch (SVNException e) {
      LOG.warn("Could not mirror svn repository", e);
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

  private SVNURL createUrlForLocalRepository() {
    try {
      return SVNURL.fromFile(context.getDirectory());
    } catch (SVNException e) {
      throw new InternalRepositoryException(repository, "could not create svn url for local repository", e);
    }
  }

  private SVNAdminClient createAdminClient(SVNURL sourceUrl, MirrorCommandRequest mirrorCommandRequest) {
    BasicAuthenticationManager authenticationManager = authenticationFactory.create(sourceUrl, mirrorCommandRequest);
    return new SVNAdminClient(authenticationManager, SVNWCUtil.createDefaultOptions(true));
  }

  private interface Worker {
    void doWork(SVNAdminClient adminClient, SVNURL sourceUrl, SVNURL targetUrl) throws SVNException;
  }
}
