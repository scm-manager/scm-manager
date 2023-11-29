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
