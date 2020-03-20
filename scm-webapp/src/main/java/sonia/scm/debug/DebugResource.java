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
    
package sonia.scm.debug;

import sonia.scm.repository.NamespaceAndName;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

/**
 * Rest api resource for the {@link DebugService}.
 * 
 * @author Sebastian Sdorra
 */
@Path("debug/{namespace}/{name}/post-receive")
public final class DebugResource
{
  private final DebugService debugService;

  /**
   * Constructs a new instance.
   * 
   * @param debugService debug service
   */
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
