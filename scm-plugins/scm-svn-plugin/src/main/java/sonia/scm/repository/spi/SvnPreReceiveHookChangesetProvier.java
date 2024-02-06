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
