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



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.wc.admin.SVNLookClient;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class SvnPreReceiveHookEvent extends AbstractRepositoryHookEvent
{

  /** the logger for SvnPreReceiveHookEvent */
  private static final Logger logger =
    LoggerFactory.getLogger(SvnPreReceiveHookEvent.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param repositoryDirectory
   * @param transaction
   */
  public SvnPreReceiveHookEvent(File repositoryDirectory, String transaction)
  {
    this.repositoryDirectory = repositoryDirectory;
    this.transaction = transaction;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Collection<Changeset> getChangesets()
  {
    if (changesets == null)
    {
      changesets = fetchChangesets();
    }

    return changesets;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public RepositoryHookType getType()
  {
    return RepositoryHookType.PRE_RECEIVE;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  private Collection<Changeset> fetchChangesets()
  {
    List<Changeset> csets = new ArrayList<Changeset>();
    SVNClientManager cm = null;

    try
    {
      ISVNOptions options = SVNWCUtil.createDefaultOptions(true);

      cm = SVNClientManager.newInstance(options);

      SVNLookClient clientManager = cm.getLookClient();
      SVNLogEntry entry = clientManager.doGetInfo(repositoryDirectory,
                            transaction);

      if (entry != null)
      {
        Changeset c = SvnUtil.createChangeset(entry);

        clientManager.doGetChanged(repositoryDirectory, transaction,
                                   new SvnModificationHandler(c), true);
        csets.add(c);
      }
      else if (logger.isWarnEnabled())
      {
        logger.warn("could not find log entry for pre receive hook");
      }
    }
    catch (Exception ex)
    {
      logger.error(
          "could not fetch changesets for transaction ".concat(transaction),
          ex);
    }
    finally
    {
      SvnUtil.dispose(cm);
    }

    return csets;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Collection<Changeset> changesets;

  /** Field description */
  private File repositoryDirectory;

  /** Field description */
  private String transaction;
}
