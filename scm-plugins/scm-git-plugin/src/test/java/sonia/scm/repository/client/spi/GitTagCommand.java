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
