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

import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.api.BrowseCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.web.VndMediaType;

import java.io.IOException;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

public class SourceRootResource {

  private final RepositoryServiceFactory serviceFactory;
  private final BrowserResultToFileObjectDtoMapper browserResultToFileObjectDtoMapper;


  @Inject
  public SourceRootResource(RepositoryServiceFactory serviceFactory, BrowserResultToFileObjectDtoMapper browserResultToFileObjectDtoMapper) {
    this.serviceFactory = serviceFactory;
    this.browserResultToFileObjectDtoMapper = browserResultToFileObjectDtoMapper;
  }

  @GET
  @Path("")
  @Produces(VndMediaType.SOURCE)
  @Operation(summary = "List of sources", description = "Returns all sources for repository head.", tags = "Repository")
  public FileObjectDto getAllWithoutRevision(@PathParam("namespace") String namespace, @PathParam("name") String name, @DefaultValue("0") @QueryParam("offset") int offset, @DefaultValue("false") @QueryParam("collapse") boolean collapse) throws IOException {
    return getSource(namespace, name, "/", null, offset, collapse);
  }

  @GET
  @Path("{revision}")
  @Produces(VndMediaType.SOURCE)
  @Operation(summary = "List of sources by revision", description = "Returns all sources for the given revision.", tags = "Repository")
  public FileObjectDto getAll(@PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("revision") String revision, @DefaultValue("0") @QueryParam("offset") int offset, @DefaultValue("false") @QueryParam("collapse") boolean collapse) throws IOException {
    return getSource(namespace, name, "/", revision, offset, collapse);
  }

  @GET
  @Path("{revision}/{path: .*}")
  @Produces(VndMediaType.SOURCE)
  @Operation(summary = "List of sources by revision in path", description = "Returns all sources for the given revision in a specific path.", tags = "Repository")
  public FileObjectDto get(@PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("revision") String revision, @PathParam("path") String path, @DefaultValue("0") @QueryParam("offset") int offset, @DefaultValue("false") @QueryParam("collapse") boolean collapse) throws IOException {
    return getSource(namespace, name, path, revision, offset, collapse);
  }

  private FileObjectDto getSource(String namespace, String repoName, String path, String revision, int offset, boolean collapse) throws IOException {
    NamespaceAndName namespaceAndName = new NamespaceAndName(namespace, repoName);
    try (RepositoryService repositoryService = serviceFactory.create(namespaceAndName)) {
      BrowseCommandBuilder browseCommand = repositoryService.getBrowseCommand();
      browseCommand.setPath(path);
      browseCommand.setOffset(offset);
      browseCommand.setCollapse(collapse);
      if (revision != null && !revision.isEmpty()) {
        browseCommand.setRevision(revision);
      }
      BrowserResult browserResult = browseCommand.getBrowserResult();

      if (browserResult != null) {
        return browserResultToFileObjectDtoMapper.map(browserResult, namespaceAndName, offset);
      } else {
        throw notFound(entity("Source", path).in("Revision", revision).in(namespaceAndName));
      }
    }
  }
}
