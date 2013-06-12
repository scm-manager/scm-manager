/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */


package sonia.scm.repository.client.spi;

//~--- non-JDK imports --------------------------------------------------------

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

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

/**
 *
 * @author Sebastian Sdorra
 */
public class GitTagCommand implements TagCommand
{

  /**
   * Constructs ...
   *
   *
   * @param git
   */
  GitTagCommand(Git git)
  {
    this.git = git;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   *
   * @throws IOException
   */
  @Override
  public Tag tag(TagRequest request) throws IOException
  {
    Tag tag = null;
    String revision = request.getRevision();

    RevObject revObject = null;

    if (!Strings.isNullOrEmpty(revision))
    {
      ObjectId id = git.getRepository().resolve(revision);
      RevWalk walk = null;

      try
      {
        walk = new RevWalk(git.getRepository());
        revObject = walk.parseAny(id);
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

      tag = new Tag(request.getName(), ref.getPeeledObjectId().toString());

    }
    catch (GitAPIException ex)
    {
      throw new RepositoryClientException("could not create tag", ex);
    }

    return tag;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Git git;
}
