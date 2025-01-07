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

import jakarta.inject.Inject;
import org.eclipse.jgit.revwalk.RevCommit;
import sonia.scm.repository.GitChangesetConverter;
import sonia.scm.repository.GitChangesetConverterFactory;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.Tag;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookContextFactory;

import java.util.List;
import java.util.function.Supplier;

import static sonia.scm.repository.RepositoryHookType.POST_RECEIVE;
import static sonia.scm.repository.RepositoryHookType.PRE_RECEIVE;

class GitRepositoryHookEventFactory {

  private final HookContextFactory hookContextFactory;
  private final GitChangesetConverterFactory changesetConverterFactory;

  @Inject
  public GitRepositoryHookEventFactory(HookContextFactory hookContextFactory, GitChangesetConverterFactory changesetConverterFactory) {
    this.hookContextFactory = hookContextFactory;
    this.changesetConverterFactory = changesetConverterFactory;
  }

  RepositoryHookEvent createPostReceiveEvent(GitContext gitContext,
                                             List<String> branches,
                                             List<Tag> tags,
                                             Supplier<Iterable<RevCommit>> changesetResolver) {
    GitChangesetConverter converter = changesetConverterFactory.create(gitContext.open());
    GitImportHookContextProvider contextProvider = new GitImportHookContextProvider(converter, branches, tags, changesetResolver);
    HookContext context = hookContextFactory.createContext(contextProvider, gitContext.getRepository());
    return new RepositoryHookEvent(context, gitContext.getRepository(), POST_RECEIVE);
  }

  RepositoryHookEvent createPreReceiveEvent(GitContext gitContext,
                                             List<String> branches,
                                             List<Tag> tags,
                                             Supplier<Iterable<RevCommit>> changesetResolver) {
    GitChangesetConverter converter = changesetConverterFactory.create(gitContext.open());
    GitImportHookContextProvider contextProvider = new GitImportHookContextProvider(converter, branches, tags, changesetResolver);
    HookContext context = hookContextFactory.createContext(contextProvider, gitContext.getRepository());
    return new RepositoryHookEvent(context, gitContext.getRepository(), PRE_RECEIVE);
  }
}
