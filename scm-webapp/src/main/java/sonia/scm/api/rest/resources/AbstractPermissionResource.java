/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */


package sonia.scm.api.rest.resources;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import sonia.scm.api.rest.Permission;
import sonia.scm.security.AssignedPermission;
import sonia.scm.security.SecuritySystem;
import sonia.scm.security.StoredAssignedPermission;

//~--- JDK imports ------------------------------------------------------------

import java.net.URI;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.31
 */
public abstract class AbstractPermissionResource
{

  /**
   * Constructs ...
   *
   *
   * @param securitySystem
   * @param name
   */
  protected AbstractPermissionResource(SecuritySystem securitySystem,
    String name)
  {
    this.securitySystem = securitySystem;
    this.name = name;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param permission
   *
   * @return
   */
  protected abstract AssignedPermission transformPermission(
    Permission permission);

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  protected abstract Predicate<AssignedPermission> getPredicate();

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param uriInfo
   * @param permission
   *
   * @return
   */
  @POST
  @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public Response add(@Context UriInfo uriInfo, Permission permission)
  {
    AssignedPermission ap = transformPermission(permission);
    StoredAssignedPermission sap = securitySystem.addPermission(ap);
    URI uri = uriInfo.getAbsolutePathBuilder().path(sap.getId()).build();

    return Response.created(uri).build();
  }

  /**
   * Method description
   *
   *
   * @param id
   *
   * @return
   */
  @DELETE
  @Path("{id}")
  public Response delete(@PathParam("id") String id)
  {
    StoredAssignedPermission sap = getPermission(id);

    securitySystem.deletePermission(sap);

    return Response.noContent().build();
  }

  /**
   * Method description
   *
   *
   * @param id
   * @param permission
   *
   * @return
   */
  @PUT
  @Path("{id}")
  @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public Response update(@PathParam("id") String id, Permission permission)
  {
    StoredAssignedPermission sap = getPermission(id);

    securitySystem.modifyPermission(new StoredAssignedPermission(sap.getId(),
      transformPermission(permission)));

    return Response.noContent().build();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param id
   *
   * @return
   */
  @GET
  @Path("{id}")
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public Permission get(@PathParam("id") String id)
  {
    StoredAssignedPermission sap = getPermission(id);

    return new Permission(sap.getId(), sap.getPermission());
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @GET
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public List<Permission> getAll()
  {
    return getPermissions(getPredicate());
  }

  /**
   * Method description
   *
   *
   * @param id
   *
   * @return
   */
  private StoredAssignedPermission getPermission(String id)
  {
    StoredAssignedPermission sap = securitySystem.getPermission(id);

    if (sap == null)
    {
      throw new WebApplicationException(Status.NOT_FOUND);
    }

    if (!getPredicate().apply(sap))
    {
      throw new WebApplicationException(Status.BAD_REQUEST);
    }

    return sap;
  }

  /**
   * Method description
   *
   *
   * @param predicate
   *
   * @return
   */
  private List<Permission> getPermissions(
    Predicate<AssignedPermission> predicate)
  {
    List<StoredAssignedPermission> permissions =
      securitySystem.getPermissions(predicate);

    return Lists.transform(permissions,
      new Function<StoredAssignedPermission, Permission>()
    {

      @Override
      public Permission apply(StoredAssignedPermission mgp)
      {
        return new Permission(mgp.getId(), mgp.getPermission());
      }
    });
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  protected String name;

  /** Field description */
  private SecuritySystem securitySystem;
}
