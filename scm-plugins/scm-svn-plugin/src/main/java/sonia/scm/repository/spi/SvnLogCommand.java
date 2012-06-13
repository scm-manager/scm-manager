/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.io.SVNRepository;

import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.SvnUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.Collection;
import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class SvnLogCommand extends AbstractSvnCommand implements LogCommand
{

  /**
   * the logger for SvnLogCommand
   */
  private static final Logger logger =
    LoggerFactory.getLogger(SvnLogCommand.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param repository
   * @param repositoryDirectory
   */
  SvnLogCommand(Repository repository, File repositoryDirectory)
  {
    super(repository, repositoryDirectory);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param revision
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public Changeset getChangeset(String revision)
          throws IOException, RepositoryException
  {
    Changeset changeset = null;

    if (logger.isDebugEnabled())
    {
      logger.debug("fetch changeset {}", revision);
    }

    SVNRepository repository = null;

    try
    {
      long revisioNumber = Long.parseLong(revision);

      repository = open();

      Collection<SVNLogEntry> entries = repository.log(null, null,
                                          revisioNumber, revisioNumber, true,
                                          true);

      if (Util.isNotEmpty(entries))
      {
        changeset = SvnUtil.createChangeset(entries.iterator().next());
      }
    }
    catch (NumberFormatException ex)
    {
      throw new RepositoryException("could not convert revision", ex);
    }
    catch (SVNException ex)
    {
      throw new RepositoryException("could not open repository", ex);
    }
    finally
    {
      SvnUtil.closeSession(repository);
    }

    return changeset;
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
    if (logger.isDebugEnabled())
    {
      logger.debug("fetch changesets for {}", request);
    }

    ChangesetPagingResult changesets = null;
    SVNRepository repository = null;
    String startRevision = request.getStartChangeset();
    String endRevision = request.getEndChangeset();

    try
    {
      repository = open();

      long startRev = repository.getLatestRevision();
      long endRev = 0;
      long maxRev = startRev;

      if (!Strings.isNullOrEmpty(startRevision))
      {
        startRev = Long.parseLong(startRevision);
      }

      if (!Strings.isNullOrEmpty(endRevision))
      {
        endRev = Long.parseLong(endRevision);
      }

      String[] pathArray = null;

      if (!Strings.isNullOrEmpty(request.getPath()))
      {
        pathArray = new String[] { request.getPath() };
      }

      List<Changeset> changesetList = Lists.newArrayList();
      Collection<SVNLogEntry> entries = repository.log(pathArray, null,
                                          startRev, endRev, true, true);

      for (SVNLogEntry entry : entries)
      {
        if (entry.getRevision() <= maxRev)
        {
          changesetList.add(SvnUtil.createChangeset(entry));
        }
      }

      int total = changesetList.size();
      int start = request.getPagingStart();
      int max = request.getPagingLimit();
      int end = total - start;

      if (end > max)
      {
        end = max;
      }

      if (start < 0)
      {
        start = 0;
      }

      changesetList = changesetList.subList(start, end);
      changesets = new ChangesetPagingResult(total, changesetList);
    }
    catch (NumberFormatException ex)
    {
      throw new RepositoryException(
          "could not parse revision ".concat(startRevision), ex);
    }
    catch (SVNException ex)
    {
      throw new RepositoryException("could not open repository", ex);
    }
    finally
    {
      SvnUtil.closeSession(repository);
    }

    return changesets;
  }
}
