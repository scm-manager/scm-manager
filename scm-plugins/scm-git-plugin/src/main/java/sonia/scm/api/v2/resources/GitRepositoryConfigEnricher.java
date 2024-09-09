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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.web.AbstractRepositoryJsonEnricher;

@Extension
public class GitRepositoryConfigEnricher extends AbstractRepositoryJsonEnricher {

  private final Provider<ScmPathInfoStore> scmPathInfoStore;
  private final RepositoryManager manager;

  @Inject
  public GitRepositoryConfigEnricher(Provider<ScmPathInfoStore> scmPathInfoStore, ObjectMapper objectMapper, RepositoryManager manager) {
    super(objectMapper);
    this.scmPathInfoStore = scmPathInfoStore;
    this.manager = manager;
  }

  @Override
  protected void enrichRepositoryNode(JsonNode repositoryNode, String namespace, String name) {
    if (GitRepositoryHandler.TYPE_NAME.equals(manager.get(new NamespaceAndName(namespace, name)).getType())) {
      String repositoryConfigLink = new LinkBuilder(scmPathInfoStore.get().get(), GitConfigResource.class)
        .method("getRepositoryConfig")
        .parameters(namespace, name)
        .href();
      addLink(repositoryNode, "configuration", repositoryConfigLink);
    }
  }
}
