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

import com.google.common.base.Strings;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.Repository;
import sonia.scm.repository.spi.javahg.HgLogChangesetCommand;

import java.util.ArrayList;
import java.util.List;

//~--- JDK imports ------------------------------------------------------------

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

  @Override
  public Changeset getChangeset(String id, LogCommandRequest request) {
    com.aragost.javahg.Repository repository = open();
    HgLogChangesetCommand cmd = on(repository);

    return cmd.rev(id).single();
  }

  @Override
  public ChangesetPagingResult getChangesets(LogCommandRequest request) {
    ChangesetPagingResult result = null;

    com.aragost.javahg.Repository repository = open();

    if (!Strings.isNullOrEmpty(request.getPath())
      ||!Strings.isNullOrEmpty(request.getBranch()))
    {
      result = collectSafely(repository, request);
    }
    else
    {

      int start = -1;
      int end = 0;

      String startChangeset = request.getStartChangeset();
      String endChangeset = request.getEndChangeset();

      if (!Strings.isNullOrEmpty(startChangeset))
      {
        start = on(repository).rev(startChangeset).singleRevision();
      }
      else if (!Strings.isNullOrEmpty(endChangeset))
      {
        end = on(repository).rev(endChangeset).singleRevision();
      }

      if (start < 0)
      {
        start = on(repository).rev("tip").singleRevision();
      }

      if (start >= 0)
      {

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

        List<Changeset> changesets = on(repository).rev(start + ":"
                                       + end).execute();

        if (request.getBranch() == null) {
          result = new ChangesetPagingResult(total, changesets);
        } else {
          result = new ChangesetPagingResult(total, changesets, request.getBranch());
        }
      }
      else
      {

        // empty repository
        result = new ChangesetPagingResult(0, new ArrayList<Changeset>());
      }
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
  private ChangesetPagingResult collectSafely(
    com.aragost.javahg.Repository repository, LogCommandRequest request)
  {
    HgLogChangesetCommand cmd = on(repository);
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

    if (!Strings.isNullOrEmpty(request.getBranch()))
    {
      cmd.branch(request.getBranch());
    }

    int start = request.getPagingStart();
    int limit = request.getPagingLimit();

    List<Changeset> changesets = null;
    int total = 0;

    if ((start == 0) && (limit < 0))
    {
      if (!Strings.isNullOrEmpty(request.getPath()))
      {
        changesets = cmd.execute(request.getPath());
      }
      else
      {
        changesets = cmd.execute();
      }

      total = changesets.size();
    }
    else
    {
      limit = limit + start;

      List<Integer> revisionList = null;

      if (!Strings.isNullOrEmpty(request.getPath()))
      {
        revisionList = cmd.loadRevisions(request.getPath());
      }
      else
      {
        revisionList = cmd.loadRevisions();
      }

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

      changesets = on(repository).rev(revs).execute();
    }

    return new ChangesetPagingResult(total, changesets);
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
  private HgLogChangesetCommand on(com.aragost.javahg.Repository repository)
  {
    return HgLogChangesetCommand.on(repository, getContext().getConfig());
  }
}
