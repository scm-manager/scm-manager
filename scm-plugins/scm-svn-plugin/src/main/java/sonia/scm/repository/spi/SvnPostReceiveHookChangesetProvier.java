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
