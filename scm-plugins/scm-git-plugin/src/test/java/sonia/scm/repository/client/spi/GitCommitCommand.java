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


import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import sonia.scm.repository.Changeset;
import sonia.scm.repository.GitChangesetConverter;
import sonia.scm.repository.GitChangesetConverterFactory;
import sonia.scm.repository.GitTestHelper;
import sonia.scm.repository.client.api.RepositoryClientException;

import java.io.IOException;


public class GitCommitCommand implements CommitCommand
{

 
  GitCommitCommand(Git git)
  {
    this.git = git;
  }



  @Override
  public Changeset commit(CommitRequest request) throws IOException
  {
    GitChangesetConverterFactory converterFactory = GitTestHelper.createConverterFactory();
    try (GitChangesetConverter converter = converterFactory.create(git.getRepository()))
    {
      RevCommit commit = git.commit()
        .setAuthor(request.getAuthor().getName(), request.getAuthor().getMail())
        .setMessage(request.getMessage())
        .call();

      return converter.createChangeset(commit);
    } catch (GitAPIException ex) {
      throw new RepositoryClientException("could not commit changes to repository", ex);
    }
  }

  //~--- fields ---------------------------------------------------------------

  private final Git git;
}
