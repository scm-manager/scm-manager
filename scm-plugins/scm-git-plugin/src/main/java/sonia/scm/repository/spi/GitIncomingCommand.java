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


import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.lib.ObjectId;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.GitChangesetConverterFactory;
import sonia.scm.repository.GitRepositoryHandler;

import java.io.IOException;


public class GitIncomingCommand extends AbstractGitIncomingOutgoingCommand
  implements IncomingCommand {

  @Inject
  GitIncomingCommand(@Assisted GitContext context, GitRepositoryHandler handler, GitChangesetConverterFactory converterFactory) {
    super(context, handler, converterFactory);
  }


  @Override
  public ChangesetPagingResult getIncomingChangesets(IncomingCommandRequest request) throws IOException {
    return getIncomingOrOutgoingChangesets(request);
  }

  @Override
  protected void prepareLogCommand(LogCommand logCommand, ObjectId localId,
    ObjectId remoteId)
    throws IOException
  {
    if (localId != null)
    {
      logCommand.not(localId);
    }

    logCommand.add(remoteId);
  }


  @Override
  protected boolean retrieveChangesets(ObjectId localId, ObjectId remoteId)
  {
    return remoteId != null;
  }

  public interface Factory {
    IncomingCommand create(GitContext context);
  }

}
