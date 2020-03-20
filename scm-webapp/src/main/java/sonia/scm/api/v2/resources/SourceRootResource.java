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

import io.swagger.v3.oas.annotations.Operation;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.api.BrowseCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.io.IOException;
import java.net.URLDecoder;

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
  public FileObjectDto getAllWithoutRevision(@PathParam("namespace") String namespace, @PathParam("name") String name, @DefaultValue("0") @QueryParam("offset") int offset) throws IOException {
    return getSource(namespace, name, "/", null, offset);
  }

  @GET
  @Path("{revision}")
  @Produces(VndMediaType.SOURCE)
  @Operation(summary = "List of sources by revision", description = "Returns all sources for the given revision.", tags = "Repository")
  public FileObjectDto getAll(@PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("revision") String revision, @DefaultValue("0") @QueryParam("offset") int offset) throws IOException {
    return getSource(namespace, name, "/", revision, offset);
  }

  @GET
  @Path("{revision}/{path: .*}")
  @Produces(VndMediaType.SOURCE)
  @Operation(summary = "List of sources by revision in path", description = "Returns all sources for the given revision in a specific path.", tags = "Repository")
  public FileObjectDto get(@PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("revision") String revision, @PathParam("path") String path, @DefaultValue("0") @QueryParam("offset") int offset) throws IOException {
    return getSource(namespace, name, path, revision, offset);
  }

  private FileObjectDto getSource(String namespace, String repoName, String path, String revision, int offset) throws IOException {
    NamespaceAndName namespaceAndName = new NamespaceAndName(namespace, repoName);
    try (RepositoryService repositoryService = serviceFactory.create(namespaceAndName)) {
      BrowseCommandBuilder browseCommand = repositoryService.getBrowseCommand();
      browseCommand.setPath(path);
      browseCommand.setOffset(offset);
      if (revision != null && !revision.isEmpty()) {
        browseCommand.setRevision(URLDecoder.decode(revision, "UTF-8"));
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
