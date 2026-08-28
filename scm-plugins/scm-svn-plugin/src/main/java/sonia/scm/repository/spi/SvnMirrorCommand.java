/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.repository.spi;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.wc.admin.SVNAdminEvent;
import org.tmatesoft.svn.core.wc.admin.SVNAdminEventAction;
import org.tmatesoft.svn.core.wc.admin.SVNAdminEventAdapter;
import org.tmatesoft.svn.core.wc.admin.SVNAdminClient;
import sonia.scm.net.GlobalProxyConfiguration;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.SvnUtil;
import sonia.scm.repository.api.MirrorCommandBuilder.LogCallback;
import sonia.scm.repository.api.MirrorCommandResult;

import javax.net.ssl.TrustManager;

import static java.util.Arrays.asList;
import static sonia.scm.repository.api.MirrorCommandResult.ResultType.FAILED;
import static sonia.scm.repository.api.MirrorCommandResult.ResultType.OK;

public class SvnMirrorCommand extends AbstractSvnCommand implements MirrorCommand {

  private static final Logger LOG = LoggerFactory.getLogger(SvnMirrorCommand.class);
  private static final String SYNCHRONIZING_REVISIONS_STEP = "Synchronizing SVN revisions";
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
      BasicAuthenticationManager authenticationManager = authenticationFactory.create(sourceUrl, mirrorCommandRequest);
      long sourceLatestRevision = getLatestRevision(sourceUrl, authenticationManager);
      SVNAdminClient admin = createAdminClient(authenticationManager);
      LogCallback progressCallback = mirrorCommandRequest.getProgressCallback();
      progressCallback.stepStarted(SYNCHRONIZING_REVISIONS_STEP, toCallbackWork(sourceLatestRevision - beforeUpdate));
      admin.setEventHandler(new ProgressCallbackEventHandler(progressCallback, beforeUpdate + 1));

      worker.doWork(admin, sourceUrl, targetUrl);

      afterUpdate = context.open().getLatestRevision();
      progressCallback.currentStepFinished();
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

  private long getLatestRevision(SVNURL sourceUrl, BasicAuthenticationManager authenticationManager) throws SVNException {
    SVNRepository sourceRepository = null;
    try {
      sourceRepository = SVNRepositoryFactory.create(sourceUrl);
      sourceRepository.setAuthenticationManager(authenticationManager);
      return sourceRepository.getLatestRevision();
    } finally {
      SvnUtil.closeSession(sourceRepository);
    }
  }

  private SVNAdminClient createAdminClient(BasicAuthenticationManager authenticationManager) {
    return new SVNAdminClient(authenticationManager, SVNWCUtil.createDefaultOptions(true));
  }

  private static int toCallbackWork(long work) {
    return (int) Math.min(Math.max(work, 0L), Integer.MAX_VALUE);
  }

  private interface Worker {
    void doWork(SVNAdminClient adminClient, SVNURL sourceUrl, SVNURL targetUrl) throws SVNException;
  }

  static class ProgressCallbackEventHandler extends SVNAdminEventAdapter {
    private final LogCallback callback;
    private final long firstExpectedRevision;

    ProgressCallbackEventHandler(LogCallback callback, long firstExpectedRevision) {
      this.callback = callback;
      this.firstExpectedRevision = firstExpectedRevision;
    }

    @Override
    public void handleAdminEvent(SVNAdminEvent event, double progress) throws SVNException {
      if (event.getAction() == SVNAdminEventAction.REVISION_PROPERTIES_COPIED) {
        long completedWork = event.getRevision() - firstExpectedRevision + 1;
        if (completedWork > 0) {
          callback.currentStepProgressed(toCallbackWork(completedWork));
        }
      }
    }
  }
}
