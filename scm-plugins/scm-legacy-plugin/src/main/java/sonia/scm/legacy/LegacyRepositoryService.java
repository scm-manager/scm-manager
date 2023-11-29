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
    
package sonia.scm.legacy;

import com.google.inject.Inject;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import sonia.scm.NotFoundException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;

@Path("v2/legacy/repository")
public class LegacyRepositoryService {

  private RepositoryManager repositoryManager;

  @Inject
  public LegacyRepositoryService(RepositoryManager repositoryManager) {
    this.repositoryManager = repositoryManager;
  }

  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
    summary = "Repository namespace and name",
    description = "Returns the repository namespace and name",
    tags = "Repository"
  )
  public NamespaceAndNameDto getNameAndNamespaceForRepositoryId(@PathParam("id") String repositoryId) {
    Repository repo = repositoryManager.get(repositoryId);
    if (repo == null) {
      throw new NotFoundException(Repository.class, repositoryId);
    }
    return new NamespaceAndNameDto(repo.getName(), repo.getNamespace());
  }
}

