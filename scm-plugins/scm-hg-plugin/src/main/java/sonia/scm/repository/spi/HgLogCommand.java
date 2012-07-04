/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Strings;

import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.spi.javahg.HgLogChangesetCommand;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class HgLogCommand extends AbstractCommand implements LogCommand
{

  /**
   * Constructs ...
   *
   *
   * @param context
   * @param repository
   */
  HgLogCommand(HgCommandContext context, Repository repository)
  {
    super(context, repository);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param id
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public Changeset getChangeset(String id)
    throws IOException, RepositoryException
  {
    com.aragost.javahg.Repository repository = open();
    HgLogChangesetCommand cmd = HgLogChangesetCommand.on(repository);

    return cmd.rev(id).single();
  }

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public ChangesetPagingResult getChangesets(LogCommandRequest request)
    throws IOException, RepositoryException
  {
    ChangesetPagingResult result = null;

    com.aragost.javahg.Repository repository = open();

    if (!Strings.isNullOrEmpty(request.getPath()))
    {
      result = collectHistory(repository, request);
    }
    else
    {

      int start = -1;
      int end = 0;

      String startChangeset = request.getStartChangeset();
      String endChangeset = request.getEndChangeset();

      if (!Strings.isNullOrEmpty(startChangeset))
      {
        start = HgLogChangesetCommand.on(repository).rev(
          startChangeset).singleRevision();
      }
      else if (!Strings.isNullOrEmpty(endChangeset))
      {
        end = HgLogChangesetCommand.on(repository).rev(
          endChangeset).singleRevision();
      }

      if (start < 0)
      {
        start =
          HgLogChangesetCommand.on(repository).rev("tip").singleRevision();
      }

      int total = start - end + 1;

      if (request.getPagingStart() > 0)
      {
        start -= request.getPagingStart();
      }

      if (request.getPagingLimit() > 0)
      {
        end = start - request.getPagingLimit() + 1;
      }

      if (end < 0)
      {
        end = 0;
      }

      List<Changeset> changesets =
        HgLogChangesetCommand.on(repository).rev(start + ":" + end).execute();

      result = new ChangesetPagingResult(total, changesets);
    }

    return result;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repository
   * @param request
   *
   * @return
   */
  private ChangesetPagingResult collectHistory(
    com.aragost.javahg.Repository repository, LogCommandRequest request)
  {
    HgLogChangesetCommand cmd = HgLogChangesetCommand.on(repository);
    String startChangeset = request.getStartChangeset();
    String endChangeset = request.getEndChangeset();

    if (!Strings.isNullOrEmpty(startChangeset)
      &&!Strings.isNullOrEmpty(endChangeset))
    {
      cmd.rev(startChangeset.concat(":").concat(endChangeset));
    }
    else if (!Strings.isNullOrEmpty(endChangeset))
    {
      cmd.rev("tip:".concat(endChangeset));
    }
    else if (!Strings.isNullOrEmpty(startChangeset))
    {
      cmd.rev(startChangeset.concat(":0"));
    }

    int start = request.getPagingStart();
    int limit = request.getPagingLimit();

    List<Changeset> changesets = null;
    int total = 0;

    if ((start == 0) && (limit < 0))
    {
      changesets = cmd.execute(request.getPath());
      total = changesets.size();
    }
    else
    {
      limit = limit + start;

      List<Integer> revisionList = cmd.loadRevisions(request.getPath());

      if ((limit > revisionList.size()) || (limit < 0))
      {
        limit = revisionList.size();
      }

      total = revisionList.size();

      List<Integer> sublist = revisionList.subList(start, limit);

      String[] revs = new String[sublist.size()];

      for (int i = 0; i < sublist.size(); i++)
      {
        revs[i] = sublist.get(i).toString();
      }

      changesets = HgLogChangesetCommand.on(repository).rev(revs).execute();
    }

    return new ChangesetPagingResult(total, changesets);
  }
}
