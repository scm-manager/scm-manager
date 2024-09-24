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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

import sonia.scm.repository.Changeset;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.SvnUtil;
import sonia.scm.util.Util;

import java.io.File;

import java.util.Collection;


public class SvnPostReceiveHookChangesetProvier
  extends AbstractSvnHookChangesetProvider
{

  
  private static final Logger logger =
    LoggerFactory.getLogger(SvnPostReceiveHookChangesetProvier.class);

  private File repositoryDirectory;

  private long revision;
 
  public SvnPostReceiveHookChangesetProvier(File repositoryDirectory, long revision)
  {
    this.repositoryDirectory = repositoryDirectory;
    this.revision = revision;
  }


  
  @Override
  public RepositoryHookType getType()
  {
    return RepositoryHookType.POST_RECEIVE;
  }


  
  @Override
  @SuppressWarnings("unchecked")
  protected Changeset fetchChangeset()
  {
    Changeset changeset = null;
    SVNRepository repository = null;

    try
    {
      repository =
        SVNRepositoryFactory.create(SVNURL.fromFile(repositoryDirectory));

      Collection<SVNLogEntry> enties = repository.log(new String[] { "" },
                                         null, revision, revision, true, true);

      if (Util.isNotEmpty(enties))
      {
        SVNLogEntry entry = enties.iterator().next();

        if (entry != null)
        {
          changeset = SvnUtil.createChangeset(entry);
        }
      }
    }
    catch (SVNException ex)
    {
      logger.error("could not open repository", ex);
    }
    finally
    {
      SvnUtil.closeSession(repository);
    }

    return changeset;
  }

}
