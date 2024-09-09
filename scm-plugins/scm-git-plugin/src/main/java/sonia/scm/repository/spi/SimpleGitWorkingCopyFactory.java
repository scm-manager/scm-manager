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

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.inject.Inject;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ScmTransportProtocol;
import sonia.scm.repository.GitWorkingCopyFactory;
import sonia.scm.repository.work.SimpleWorkingCopyFactory;
import sonia.scm.repository.work.WorkingCopyPool;
import sonia.scm.util.SystemUtil;

import java.io.File;

public class SimpleGitWorkingCopyFactory extends SimpleWorkingCopyFactory<Repository, Repository, GitContext> implements GitWorkingCopyFactory {

  @Inject
  public SimpleGitWorkingCopyFactory(WorkingCopyPool workdirProvider, MeterRegistry meterRegistry) {
    super(workdirProvider, meterRegistry);
  }

  @Override
  public ParentAndClone<Repository, Repository> initialize(GitContext context, File target, String initialBranch) {
    return new GitWorkingCopyInitializer(this, context).initialize(target, initialBranch);
  }

  @Override
  public ParentAndClone<Repository, Repository> reclaim(GitContext context, File target, String initialBranch) throws SimpleWorkingCopyFactory.ReclaimFailedException {
    return new GitWorkingCopyReclaimer(context).reclaim(target, initialBranch);
  }

  String createScmTransportProtocolUri(File bareRepository) {
    if (SystemUtil.isWindows()) {
      return ScmTransportProtocol.NAME + ":///" + bareRepository.getAbsolutePath().replaceAll("\\\\", "/");
    } else {
      return ScmTransportProtocol.NAME + "://" + bareRepository.getAbsolutePath();
    }
  }

  @Override
  protected void closeRepository(Repository repository) {
    // we have to check for null here, because we do not create a repository for
    // the parent in cloneRepository
    if (repository != null) {
      repository.close();
    }
  }

  @Override
  protected void closeWorkingCopy(Repository workingCopy) {
    if (workingCopy != null) {
      workingCopy.close();
    }
  }

}
