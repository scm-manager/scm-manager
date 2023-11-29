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

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.ws.rs.Path;

/**
 * RESTful Web Service Resource to manage repositories.
 */
@OpenAPIDefinition(tags = {
  @Tag(name = "Repository", description = "Repository related endpoints")
})
@Path(RepositoryRootResource.REPOSITORIES_PATH_V2)
public class RepositoryRootResource {
  static final String REPOSITORIES_PATH_V2 = "v2/repositories/";

  private final Provider<RepositoryResource> repositoryResource;
  private final Provider<RepositoryCollectionResource> repositoryCollectionResource;
  private final Provider<RepositoryImportResource> repositoryImportResource;

  @Inject
  public RepositoryRootResource(Provider<RepositoryResource> repositoryResource, Provider<RepositoryCollectionResource> repositoryCollectionResource, Provider<RepositoryImportResource> repositoryImportResource) {
    this.repositoryResource = repositoryResource;
    this.repositoryCollectionResource = repositoryCollectionResource;
    this.repositoryImportResource = repositoryImportResource;
  }

  @Path("{namespace}/{name}")
  public RepositoryResource getRepositoryResource() {
    return repositoryResource.get();
  }

  @Path("")
  public RepositoryCollectionResource getRepositoryCollectionResource() {
    return repositoryCollectionResource.get();
  }

  @Path("import")
  public RepositoryImportResource getRepositoryImportResource() {
    return repositoryImportResource.get();
  }
}
