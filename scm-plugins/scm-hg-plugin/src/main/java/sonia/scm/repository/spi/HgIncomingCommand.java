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
import org.javahg.commands.ExecutionException;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.spi.javahg.HgIncomingChangesetCommand;

import java.io.File;
import java.util.Collections;
import java.util.List;


public class HgIncomingCommand extends AbstractCommand
  implements IncomingCommand
{

  private static final int NO_INCOMING_CHANGESETS = 1;

  private HgRepositoryHandler handler;

  @Inject
  HgIncomingCommand(@Assisted HgCommandContext context, HgRepositoryHandler handler)
  {
    super(context);
    this.handler = handler;
  }


  @Override
  @SuppressWarnings("unchecked")
  public ChangesetPagingResult getIncomingChangesets(IncomingCommandRequest request) {
    File remoteRepository = handler.getDirectory(request.getRemoteRepository().getId());

    org.javahg.Repository repository = open();

    // TODO implement paging

    List<Changeset> changesets;

    try
    {

      changesets = on(repository).execute(remoteRepository.getAbsolutePath());

    }
    catch (ExecutionException ex)
    {
      if (ex.getCommand().getReturnCode() == NO_INCOMING_CHANGESETS)
      {
        changesets = Collections.emptyList();
      }
      else
      {
        throw new InternalRepositoryException(getRepository(), "could not execute incoming command", ex);
      }
    }

    return new ChangesetPagingResult(changesets.size(), changesets);
  }



  private HgIncomingChangesetCommand on(
    org.javahg.Repository repository)
  {
    return HgIncomingChangesetCommand.on(repository, getContext().getConfig());
  }

  public interface Factory {
    HgIncomingCommand create(HgCommandContext context);
  }

}
