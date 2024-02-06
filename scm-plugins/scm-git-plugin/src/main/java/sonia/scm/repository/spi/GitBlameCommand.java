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
