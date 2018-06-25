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
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.ResponseHeader;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;

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
 * Abstract base class for global permission resources.
 *
 * @author Sebastian Sdorra
 * @since 1.31
 */
public abstract class AbstractPermissionResource
{

  /**
   * Constructs a new {@link AbstractPermissionResource}.
   *
   *
   * @param securitySystem security system
   * @param name name of the user or group
   */
  protected AbstractPermissionResource(SecuritySystem securitySystem,
    String name)
  {
    this.securitySystem = securitySystem;
    this.name = name;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Transforms a {@link Permission} to a {@link AssignedPermission}.
   *
   *
   * @param permission permission object to transform
   *
   * @return transformed {@link AssignedPermission}
   */
  protected abstract AssignedPermission transformPermission(
    Permission permission);

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns a {@link Predicate} to filter permissions.
   *
   *
   * @return {@link Predicate} to filter permissions
   */
  protected abstract Predicate<AssignedPermission> getPredicate();

  //~--- methods --------------------------------------------------------------

  /**
   * Adds a new permission to the user or group managed by the resource.
   *
   * @param uriInfo uri informations
   * @param permission permission to add
   *
   * @return web response
   */
  @POST
  @StatusCodes({
    @ResponseCode(code = 201, condition = "creates", additionalHeaders = {
      @ResponseHeader(name = "Location", description = "uri to new create permission")
    }),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public Response add(@Context UriInfo uriInfo, Permission permission)
  {
    AssignedPermission ap = transformPermission(permission);
    StoredAssignedPermission sap = securitySystem.addPermission(ap);
    URI uri = uriInfo.getAbsolutePathBuilder().path(sap.getId()).build();

    return Response.created(uri).build();
  }

  /**
   * Deletes a permission from the user or group managed by the resource.
   *
   * @param id id of the permission
   *
   * @return web response
   */
  @DELETE
  @Path("{id}")
  @StatusCodes({
    @ResponseCode(code = 204, condition = "success"),
    @ResponseCode(code = 400, condition = "bad request, permission id does not belong to the user or group"),
    @ResponseCode(code = 404, condition = "not found, no permission with the specified id available"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  public Response delete(@PathParam("id") String id)
  {
    StoredAssignedPermission sap = getPermission(id);

    securitySystem.deletePermission(sap);

    return Response.noContent().build();
  }

  /**
   * Updates the specified permission on the user or group managed by the resource.
   *
   * @param id id of the permission
   * @param permission updated permission
   *
   * @return web response
   */
  @PUT
  @Path("{id}")
  @StatusCodes({
    @ResponseCode(code = 204, condition = "success"),
    @ResponseCode(code = 400, condition = "bad request, permission id does not belong to the user or group"),
    @ResponseCode(code = 404, condition = "not found, no permission with the specified id available"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
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
   * Returns the {@link Permission} with the specified id.
   *
   * @param id id of the {@link Permission}
   *
   * @return {@link Permission} with the specified id
   */
  @GET
  @Path("{id}")
  @StatusCodes({
    @ResponseCode(code = 204, condition = "success"),
    @ResponseCode(code = 400, condition = "bad request, permission id does not belong to the user or group"),
    @ResponseCode(code = 404, condition = "not found, no permission with the specified id available"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public Permission get(@PathParam("id") String id)
  {
    StoredAssignedPermission sap = getPermission(id);

    return new Permission(sap.getId(), sap.getPermission());
  }

  /**
   * Returns all permissions of the user or group managed by the resource.
   *
   * @return all permissions of the user or group
   */
  @GET
  @StatusCodes({
    @ResponseCode(code = 204, condition = "success"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public List<Permission> getAll()
  {
    return getPermissions(getPredicate());
  }

  /**
   * Returns the {@link StoredAssignedPermission} with the given id.
   *
   *
   * @param id id of the stored permission
   *
   * @return {@link StoredAssignedPermission} with the given id
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
   * Returns all permissions which matches the given {@link Predicate}.
   *
   *
   * @param predicate predicate for filtering
   *
   * @return all permissions which matches the given {@link Predicate}
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

  /** name of the user or the group */
  protected String name;

  /** security system */
  private SecuritySystem securitySystem;
}
