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

package sonia.scm.repository;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.io.fs.FSHook;
import org.tmatesoft.svn.core.internal.io.fs.FSHookEvent;
import org.tmatesoft.svn.core.internal.io.fs.FSHooks;
import sonia.scm.repository.spi.AbstractSvnHookChangesetProvider;
import sonia.scm.repository.spi.HookEventFacade;
import sonia.scm.repository.spi.SvnHookContextProvider;
import sonia.scm.repository.spi.SvnPostReceiveHookChangesetProvier;
import sonia.scm.repository.spi.SvnPreReceiveHookChangesetProvier;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.Util;

import java.io.File;
import java.io.IOException;


public class SvnRepositoryHook implements FSHook
{

  private static final Logger logger =
    LoggerFactory.getLogger(SvnRepositoryHook.class);

  private HookEventFacade hookEventFacade;

  private final SvnRepositoryHandler handler;

  public SvnRepositoryHook(HookEventFacade hookEventFacade, SvnRepositoryHandler handler)
  {
    this.hookEventFacade = hookEventFacade;
    this.handler = handler;
  }


  
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

          fireHook(directory,
            new SvnPostReceiveHookChangesetProvier(directory, revision));
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
          fireHook(directory,
            new SvnPreReceiveHookChangesetProvier(directory, tx));
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


  private void fireHook(File directory,
    AbstractSvnHookChangesetProvider changesetProvider)
    throws SVNCancelException
  {
    try
    {
      String repositoryId = getRepositoryId(directory);

      //J-
      hookEventFacade.handle(repositoryId)
        .fireHookEvent(
          changesetProvider.getType(),
          new SvnHookContextProvider(changesetProvider)
        );
      //J+
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



  private String getRepositoryId(File directory)
  {
    AssertUtil.assertIsNotNull(directory);
    return handler.getRepositoryId(directory);
  }

}
