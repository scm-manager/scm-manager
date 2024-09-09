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
import org.javahg.BaseRepository;
import org.javahg.Repository;
import org.javahg.commands.CloneCommand;
import org.javahg.commands.ExecutionException;
import org.javahg.commands.PullCommand;
import org.javahg.commands.StatusCommand;
import org.javahg.commands.UpdateCommand;
import org.javahg.commands.flags.CloneCommandFlags;
import org.javahg.ext.purge.PurgeCommand;
import sonia.scm.repository.HgExtensions;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.work.SimpleWorkingCopyFactory;
import sonia.scm.repository.work.WorkingCopyPool;
import sonia.scm.util.IOUtil;

import java.io.File;
import java.io.IOException;

public class SimpleHgWorkingCopyFactory extends SimpleWorkingCopyFactory<Repository, Repository, HgCommandContext> implements HgWorkingCopyFactory {

  @Inject
  public SimpleHgWorkingCopyFactory(WorkingCopyPool workdirProvider, MeterRegistry meterRegistry) {
    super(workdirProvider, meterRegistry);
  }

  @Override
  public ParentAndClone<Repository, Repository> initialize(HgCommandContext context, File target, String initialBranch) {
    Repository centralRepository = context.openForWrite();
    CloneCommand cloneCommand = CloneCommandFlags.on(centralRepository);
    if (initialBranch != null) {
      cloneCommand.updaterev(initialBranch);
    }
    try {
      cloneCommand.execute(target.getAbsolutePath());
    } catch (IOException e) {
      throw new InternalRepositoryException(context.getScmRepository(), "could not clone repository", e);
    }

    BaseRepository clone = Repository.open(target);

    return new ParentAndClone<>(centralRepository, clone, target);
  }

  @Override
  // The hg api to create a command is meant to be used from the command classes, not from their "flags" base classes.
  @SuppressWarnings("java:S3252")
  protected ParentAndClone<Repository, Repository> reclaim(HgCommandContext context, File target, String initialBranch) throws ReclaimFailedException {
    Repository centralRepository = context.openForWrite();
    try {
      BaseRepository clone = Repository.open(target);
      for (String unknown : StatusCommand.on(clone).execute().getUnknown()) {
        delete(clone.getDirectory(), unknown);
      }
      String branchToCheckOut = initialBranch == null ? "default" : initialBranch;
      UpdateCommand.on(clone).rev(branchToCheckOut).clean().execute();
      PurgeCommand.on(clone).execute();
      return new ParentAndClone<>(centralRepository, clone, target);
    } catch (ExecutionException | IOException e) {
      throw new ReclaimFailedException(e);
    }
  }

  private void delete(File directory, String unknownFile) throws IOException {
    IOUtil.delete(new File(directory, unknownFile));
  }

  @Override
  protected void closeRepository(Repository repository) {
    repository.close();
  }

  @Override
  protected void closeWorkingCopy(Repository workingCopy) {
    workingCopy.close();
  }

  @Override
  public void configure(PullCommand pullCommand) {
    String hooks = HgExtensions.HOOK.getFile().getAbsolutePath();
    pullCommand.cmdAppend("--config", String.format("hooks.pretxnchangegroup.scm=python:%s:pre_hook", hooks));
    pullCommand.cmdAppend("--config", String.format("hooks.changegroup.scm=python:%s:post_hook", hooks));
  }
}
