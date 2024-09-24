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
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.wc.admin.SVNLookClient;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.SvnUtil;

import java.io.File;


public class SvnPreReceiveHookChangesetProvier
  extends AbstractSvnHookChangesetProvider
{
  private static final Logger logger =
    LoggerFactory.getLogger(SvnPreReceiveHookChangesetProvier.class);

  private File repositoryDirectory;

  private String transaction;
 
  public SvnPreReceiveHookChangesetProvier(File repositoryDirectory,
    String transaction)
  {
    this.repositoryDirectory = repositoryDirectory;
    this.transaction = transaction;
  }


  
  @Override
  public RepositoryHookType getType()
  {
    return RepositoryHookType.PRE_RECEIVE;
  }


  
  @Override
  protected Changeset fetchChangeset()
  {
    Changeset changeset = null;
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
        changeset = SvnUtil.createChangeset(entry);
        changeset.setId(SvnUtil.createTransactionEntryId(transaction));
      }
      else if (logger.isWarnEnabled())
      {
        logger.warn("could not find log entry for pre receive hook");
      }
    }
    catch (Exception ex)
    {
      logger.error(
        "could not fetch changesets for transaction ".concat(transaction), ex);
    }
    finally
    {
      SvnUtil.dispose(cm);
    }

    return changeset;
  }

}
