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

//~--- non-JDK imports --------------------------------------------------------

import org.javahg.commands.ExecutionException;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.spi.javahg.HgIncomingChangesetCommand;

import javax.inject.Inject;
import java.io.File;
import java.util.Collections;
import java.util.List;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public class HgIncomingCommand extends AbstractCommand
  implements IncomingCommand
{

  /** Field description */
  private static final int NO_INCOMING_CHANGESETS = 1;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *  @param context
   * @param handler
   */
  @Inject
  HgIncomingCommand(HgCommandContext context, HgRepositoryHandler handler)
  {
    super(context);
    this.handler = handler;
  }

  //~--- get methods ----------------------------------------------------------

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

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
  private HgIncomingChangesetCommand on(
    org.javahg.Repository repository)
  {
    return HgIncomingChangesetCommand.on(repository, getContext().getConfig());
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private HgRepositoryHandler handler;
}
