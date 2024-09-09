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

import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.Tag;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookContextFactory;

import java.util.List;

import static sonia.scm.repository.RepositoryHookType.POST_RECEIVE;
import static sonia.scm.repository.spi.HgBranchesTagsExtractor.extractBranches;
import static sonia.scm.repository.spi.HgBranchesTagsExtractor.extractTags;

public class HgRepositoryHookEventFactory {

  private final HookContextFactory hookContextFactory;

  @Inject
  public HgRepositoryHookEventFactory(HookContextFactory hookContextFactory) {
    this.hookContextFactory = hookContextFactory;
  }

  RepositoryHookEvent createEvent(@Assisted HgCommandContext hgContext, HgLazyChangesetResolver changesetResolver) {
    List<String> branches = extractBranches(hgContext);
    List<Tag> tags = extractTags(hgContext);
    HgImportHookContextProvider contextProvider = new HgImportHookContextProvider(branches, tags, changesetResolver);
    HookContext context = hookContextFactory.createContext(contextProvider, hgContext.getScmRepository());
    return new RepositoryHookEvent(context, hgContext.getScmRepository(), POST_RECEIVE);
  }

  public interface Factory {
    HgRepositoryHookEventFactory create(HgCommandContext context);
  }

}
