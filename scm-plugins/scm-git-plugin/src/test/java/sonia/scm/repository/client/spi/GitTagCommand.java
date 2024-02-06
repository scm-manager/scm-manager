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

package sonia.scm.repository.client.spi;


import com.google.common.base.Strings;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevWalk;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.Tag;
import sonia.scm.repository.client.api.RepositoryClientException;

import java.io.IOException;


public class GitTagCommand implements TagCommand
{

 
  GitTagCommand(Git git)
  {
    this.git = git;
  }



  @Override
  public Tag tag(TagRequest request) throws IOException
  {
    Tag tag = null;
    String revision = request.getRevision();

    RevObject revObject = null;
    Long tagTime = null;

    if (!Strings.isNullOrEmpty(revision))
    {
      ObjectId id = git.getRepository().resolve(revision);
      RevWalk walk = null;

      try
      {
        walk = new RevWalk(git.getRepository());
        revObject = walk.parseAny(id);
        tagTime = GitUtil.getTagTime(walk, id);
      }
      finally
      {
        GitUtil.release(walk);
      }
    }

    try
    {
      Ref ref = null;

      if (revObject != null)
      {
        ref =
          git.tag().setObjectId(revObject).setName(request.getName()).call();
      }
      else
      {
        ref = git.tag().setObjectId(revObject).call();
      }

      if (ref.isPeeled()) {
        tag = new Tag(request.getName(), ref.getPeeledObjectId().toString(), tagTime);
      } else {
        tag = new Tag(request.getName(), ref.getObjectId().toString(), tagTime);
      }

    }
    catch (GitAPIException ex)
    {
      throw new RepositoryClientException("could not create tag", ex);
    }

    return tag;
  }

  //~--- fields ---------------------------------------------------------------

  private Git git;
}
