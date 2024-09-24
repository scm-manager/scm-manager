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

package sonia.scm.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import static java.util.Collections.singletonMap;
import static sonia.scm.web.VndMediaType.REPOSITORY;
import static sonia.scm.web.VndMediaType.REPOSITORY_COLLECTION;

@Slf4j
public abstract class AbstractRepositoryJsonEnricher extends JsonEnricherBase {

  public AbstractRepositoryJsonEnricher(ObjectMapper objectMapper) {
    super(objectMapper);
  }

  @Override
  public void enrich(JsonEnricherContext context) {
    if (resultHasMediaType(REPOSITORY, context)) {
      JsonNode repositoryNode = context.getResponseEntity();
      enrichRepositoryNode(repositoryNode);
    } else if (resultHasMediaType(REPOSITORY_COLLECTION, context)) {
      JsonNode repositoryCollectionNode = context.getResponseEntity().get("_embedded").withArray("repositories");
      repositoryCollectionNode.elements().forEachRemaining(this::enrichRepositoryNode);
    }
  }

  private void enrichRepositoryNode(JsonNode repositoryNode) {
    String namespace = repositoryNode.get("namespace").asText();
    String name = repositoryNode.get("name").asText();

    try {
      enrichRepositoryNode(repositoryNode, namespace, name);
    } catch (Exception e) {
      log.warn("failed to enrich repository; it might be, that the repository has been deleted in the meantime", e);
    }
  }

  protected abstract void enrichRepositoryNode(JsonNode repositoryNode, String namespace, String name);

  protected void addLink(JsonNode repositoryNode, String linkName, String link) {
    JsonNode hrefNode = createObject(singletonMap("href", value(link)));
    addPropertyNode(repositoryNode.get("_links"), linkName, hrefNode);
  }
}
