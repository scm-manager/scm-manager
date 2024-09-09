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

import org.javahg.Changeset;
import org.javahg.Repository;
import org.javahg.commands.ExecutionException;
import org.javahg.commands.PullCommand;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.work.WorkingCopy;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public class AbstractWorkingCopyCommand extends AbstractCommand {
  static final Pattern HG_MESSAGE_PATTERN = Pattern.compile(".*\\[SCM\\](?: Error:)? (.*)");

  protected final HgWorkingCopyFactory workingCopyFactory;

  public AbstractWorkingCopyCommand(HgCommandContext context, HgWorkingCopyFactory workingCopyFactory)
  {
    super(context);
    this.workingCopyFactory = workingCopyFactory;
  }

  protected List<Changeset> pullChangesIntoCentralRepository(WorkingCopy<Repository, Repository> workingCopy, String branch) {
    try {
      org.javahg.commands.PullCommand pullCommand = PullCommand.on(workingCopy.getCentralRepository());
      workingCopyFactory.configure(pullCommand);
      return pullCommand.execute(workingCopy.getDirectory().getAbsolutePath());
    } catch (ExecutionException e) {
      throw IntegrateChangesFromWorkdirException
        .withPattern(HG_MESSAGE_PATTERN)
        .forMessage(context.getScmRepository(), e.getMessage());
    } catch (IOException e) {
      throw new InternalRepositoryException(getRepository(),
        String.format("Could not pull changes '%s' into central repository", branch),
        e);
    }
  }
}
