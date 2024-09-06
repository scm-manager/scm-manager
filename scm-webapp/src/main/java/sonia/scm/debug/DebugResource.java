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

package sonia.scm.debug;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import sonia.scm.repository.NamespaceAndName;

import java.util.Collection;

/**
 * Rest api resource for the {@link DebugService}.
 * 
 */
@Path("debug/{namespace}/{name}/post-receive")
public final class DebugResource
{
  private final DebugService debugService;

  @Inject
  public DebugResource(DebugService debugService)
  {
    this.debugService = debugService;
  }
  
  /**
   * Returns all received hook data for the given repository.
   * 
   * @param namespace repository namespace
   * @param name repository name
   *
   * @return all received hook data for the given repository
   */
  @GET
  @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
  public Collection<DebugHookData> getAll(@PathParam("namespace") String namespace, @PathParam("name") String name){
    return debugService.getAll(new NamespaceAndName(namespace, name));
  }
  
  /**
   * Returns the last received hook data for the given repository.
   *
   * @param namespace repository namespace
   * @param name repository name
   * 
   * @return the last received hook data for the given repository
   */
  @GET
  @Path("last")
  @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
  public DebugHookData getLast(@PathParam("namespace") String namespace, @PathParam("name") String name){
    return debugService.getLast(new NamespaceAndName(namespace, name));
  }
  
}
