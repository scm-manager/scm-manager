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

import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.io.fs.FSHook;
import org.tmatesoft.svn.core.internal.io.fs.FSHookEvent;
import org.tmatesoft.svn.core.internal.io.fs.FSHooks;

import sonia.scm.util.AssertUtil;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Sebastian Sdorra
 */
public class SvnRepositoryHook implements FSHook
{

  /** the logger for SvnRepositoryHook */
  private static final Logger logger =
    LoggerFactory.getLogger(SvnRepositoryHook.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param repositoryManager
   * @param handler
   */
  public SvnRepositoryHook(RepositoryManager repositoryManager,
                           SvnRepositoryHandler handler)
  {
    this.repositoryManager = repositoryManager;
    this.handler = handler;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param event
   *
   * @throws SVNException
   */
  @Override
  public void onHook(FSHookEvent event) throws SVNException
  {
    File directory = event.getReposRootDir();

    if (FSHooks.SVN_REPOS_HOOK_POST_COMMIT.equals(event.getType()))
    {
      String[] args = event.getArgs();

      if (Util.isNotEmpty(args))
      {
        try
        {
          long revision = Long.parseLong(args[0]);

          fireHook(directory, new SvnPostReceiveHookEvent(directory, revision));
        }
        catch (NumberFormatException ex)
        {
          logger.error("could not parse revision of post commit hook", ex);
        }
      }
      else if (logger.isWarnEnabled())
      {
        logger.warn("no arguments found on post commit hook");
      }
    }
    else if (FSHooks.SVN_REPOS_HOOK_PRE_COMMIT.equals(event.getType()))
    {
      String[] args = event.getArgs();

      if (Util.isNotEmpty(args))
      {
        String tx = args[0];

        if (Util.isNotEmpty(tx))
        {
          fireHook(directory, new SvnPreReceiveHookEvent(directory, tx));
        }
        else if (logger.isWarnEnabled())
        {
          logger.warn("transaction of pre commit hook is empty");
        }
      }
      else if (logger.isWarnEnabled())
      {
        logger.warn("no arguments found on pre commit hook");
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param directory
   * @param hookEvent
   *
   * @throws SVNCancelException
   */
  private void fireHook(File directory, RepositoryHookEvent hookEvent)
          throws SVNCancelException
  {
    try
    {
      String name = getRepositoryName(directory);

      name = IOUtil.trimSeperatorChars(name);
      repositoryManager.fireHookEvent(SvnRepositoryHandler.TYPE_NAME, name,
                                      hookEvent);
    }
    catch (Exception ex)
    {
      if (logger.isWarnEnabled())
      {
        logger.warn("error during hook execution", ex);
      }

      throw new SVNCancelException(
          SVNErrorMessage.create(SVNErrorCode.CANCELLED, ex.getMessage()));
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param directory
   *
   * @return
   *
   * @throws IOException
   */
  private String getRepositoryName(File directory) throws IOException
  {
    AssertUtil.assertIsNotNull(directory);

    String name = null;
    String path = directory.getCanonicalPath();
    int directoryLength =
      handler.getConfig().getRepositoryDirectory().getCanonicalPath().length();

    if (directoryLength < path.length())
    {
      name = IOUtil.trimSeperatorChars(path.substring(directoryLength));
    }
    else if (logger.isWarnEnabled())
    {
      logger.warn("path is shorter as the main hg repository path");
    }

    return name;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private SvnRepositoryHandler handler;

  /** Field description */
  private RepositoryManager repositoryManager;
}
