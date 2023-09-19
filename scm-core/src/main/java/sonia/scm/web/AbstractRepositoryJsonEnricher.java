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
