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


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.BlameLine;
import sonia.scm.repository.BlameResult;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Person;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static sonia.scm.ContextEntry.ContextBuilder.entity;


public class GitBlameCommand extends AbstractGitCommand implements BlameCommand
{

 
  private static final Logger logger =
    LoggerFactory.getLogger(GitBlameCommand.class);


  @Inject
  public GitBlameCommand(@Assisted GitContext context)
  {
    super(context);
  }


  @Override
  public BlameResult getBlameResult(BlameCommandRequest request)
          throws IOException
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("try to create blame for {}", request);
    }

    Preconditions.checkArgument(!Strings.isNullOrEmpty(request.getPath()),
                                "path is empty or null");

    BlameResult result = null;

    try
    {
      org.eclipse.jgit.lib.Repository gr = open();
      org.eclipse.jgit.api.BlameCommand blame = new Git(gr).blame();

      blame.setFilePath(request.getPath());

      ObjectId revId = getCommitOrDefault(gr, request.getRevision());

      blame.setStartCommit(revId);

      org.eclipse.jgit.blame.BlameResult gitBlameResult = blame.call();

      if (gitBlameResult == null)
      {
        throw new InternalRepositoryException(entity("Path", request.getPath()).in(repository),
            "could not create blame result for path");
      }

      List<BlameLine> blameLines = new ArrayList<BlameLine>();
      int total = gitBlameResult.getResultContents().size();
      int i = 0;

      for (; i < total; i++)
      {
        RevCommit commit = gitBlameResult.getSourceCommit(i);

        if (commit != null)
        {
          PersonIdent author = gitBlameResult.getSourceAuthor(i);
          BlameLine blameLine = new BlameLine();

          blameLine.setLineNumber(i + 1);
          blameLine.setAuthor(new Person(author.getName(),
                                         author.getEmailAddress()));
          blameLine.setDescription(commit.getShortMessage());

          long when = GitUtil.getCommitTime(commit);

          blameLine.setWhen(when);

          String rev = commit.getId().getName();

          blameLine.setRevision(rev);

          String content = gitBlameResult.getResultContents().getString(i);

          blameLine.setCode(content);
          blameLines.add(blameLine);
        }
      }

      result = new BlameResult(i, blameLines);
    }
    catch (GitAPIException ex)
    {
      throw new InternalRepositoryException(repository, "could not create blame view", ex);
    }

    return result;
  }

  public interface Factory {
    BlameCommand create(GitContext context);
  }

}
