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

