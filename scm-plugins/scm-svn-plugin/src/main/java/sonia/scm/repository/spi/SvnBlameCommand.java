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
