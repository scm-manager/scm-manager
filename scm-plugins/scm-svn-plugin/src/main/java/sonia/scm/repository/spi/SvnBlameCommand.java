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


import com.google.common.collect.Lists;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNLogClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import sonia.scm.repository.BlameLine;
import sonia.scm.repository.BlameResult;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.SvnBlameHandler;
import sonia.scm.util.Util;

import java.io.File;
import java.util.List;


public class SvnBlameCommand extends AbstractSvnCommand implements BlameCommand
{

  public SvnBlameCommand(SvnContext context)
  {
    super(context);
  }

  @Override
  public BlameResult getBlameResult(BlameCommandRequest request) {
    String path = request.getPath();
    String revision = request.getRevision();
    List<BlameLine> blameLines = Lists.newArrayList();
    SVNRevision endRevision = null;

    if (Util.isNotEmpty(revision))
    {
      endRevision = SVNRevision.create(Long.parseLong(revision));
    }
    else
    {
      endRevision = SVNRevision.HEAD;
    }

    try
    {
      SVNURL svnurl = SVNURL.fromFile(new File(context.getDirectory(), path));
      SVNRepository svnRepository =
        SVNRepositoryFactory.create(context.createUrl());
      ISVNAuthenticationManager svnManager =
        svnRepository.getAuthenticationManager();
      SVNLogClient svnLogClient = new SVNLogClient(svnManager, null);

      svnLogClient.doAnnotate(svnurl, SVNRevision.UNDEFINED,
                              SVNRevision.create(1l), endRevision,
                              new SvnBlameHandler(svnRepository, path,
                                blameLines));
    }
    catch (SVNException ex)
    {
      throw new InternalRepositoryException(repository, "could not create blame result", ex);
    }

    return new BlameResult(blameLines.size(), blameLines);
  }
}
