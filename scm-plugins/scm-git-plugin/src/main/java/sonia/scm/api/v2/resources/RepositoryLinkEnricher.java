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

package sonia.scm.api.v2.resources;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;

@Extension
@Enrich(Repository.class)
public class RepositoryLinkEnricher implements HalEnricher {

  private final Provider<ScmPathInfoStore> scmPathInfoStore;

  @Inject
  public RepositoryLinkEnricher(Provider<ScmPathInfoStore> scmPathInfoStore) {
    this.scmPathInfoStore = scmPathInfoStore;
  }

  @Override
  public void enrich(HalEnricherContext context, HalAppender appender) {
    Repository repository = context.oneRequireByType(Repository.class);

    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get().get(), GitConfigResource.class, GitRepositoryConfigResource.class);

    if (RepositoryPermissions.read(repository).isPermitted() && repository.getType().equals("git")) {
      appender.appendLink("defaultBranch", getDefaultBranchLink(repository, linkBuilder));
    }
  }

  private String getDefaultBranchLink(Repository repository, LinkBuilder linkBuilder) {
    return linkBuilder
      .method("getRepositoryConfig").parameters(repository.getNamespace(), repository.getName())
      .method("getDefaultBranch").parameters()
      .href();
  }
}
