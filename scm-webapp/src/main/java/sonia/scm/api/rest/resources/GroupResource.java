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

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.codehaus.enunciate.jaxrs.TypeHint;
import org.codehaus.enunciate.modules.jersey.SpringManagedLifecycle;

import sonia.scm.group.Group;
import sonia.scm.group.GroupException;
import sonia.scm.group.GroupManager;
import sonia.scm.util.SecurityUtil;
import sonia.scm.web.security.WebSecurityContext;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Sebastian Sdorra
 */
@Path("groups")
@Singleton
@SpringManagedLifecycle
public class GroupResource
        extends AbstractManagerResource<Group, GroupException>
{

  /** Field description */
  public static final String PATH_PART = "groups";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   * @param securityContextProvider
   * @param groupManager
   */
  @Inject
  public GroupResource(Provider<WebSecurityContext> securityContextProvider,
                       GroupManager groupManager)
  {
    super(groupManager);
    this.securityContextProvider = securityContextProvider;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   * @param uriInfo
   * @param group
   *
   * @return
   */
  @POST
  @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  @Override
  public Response create(@Context UriInfo uriInfo, Group group)
  {
    return super.create(uriInfo, group);
  }

  /**
   * Method description
   *
   *
   * @param name
   *
   * @return
   */
  @DELETE
  @Path("{id}")
  @Override
  public Response delete(@PathParam("id") String name)
  {
    return super.delete(name);
  }

  /**
   * Method description
   *
   *
   * @param uriInfo
   * @param name
   * @param group
   *
   * @return
   */
  @PUT
  @Path("{id}")
  @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  @Override
  public Response update(@Context UriInfo uriInfo,
                         @PathParam("id") String name, Group group)
  {
    return super.update(uriInfo, name, group);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   * @param id
   *
   * @return
   */
  @GET
  @Path("{id}")
  @TypeHint(Group.class)
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  @Override
  public Response get(@Context Request request, @PathParam("id") String id)
  {
    Response response = null;

    if (SecurityUtil.isAdmin(securityContextProvider))
    {
      response = super.get(request, id);
    }
    else
    {
      response = Response.status(Response.Status.FORBIDDEN).build();
    }

    return response;
  }

  /**
   * Method description
   *
   *
   * @param request
   * @param start
   * @param limit
   * @param sortby
   * @param desc
   *
   * @return
   */
  @GET
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  @TypeHint(Group[].class)
  @Override
  public Response getAll(@Context Request request, @DefaultValue("0")
  @QueryParam("start") int start, @DefaultValue("-1")
  @QueryParam("limit") int limit, @QueryParam("sortby") String sortby,
                                  @DefaultValue("false")
  @QueryParam("desc") boolean desc)
  {
    return super.getAll(request, start, limit, sortby, desc);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param items
   *
   * @return
   */
  @Override
  protected GenericEntity<Collection<Group>> createGenericEntity(
          Collection<Group> items)
  {
    return new GenericEntity<Collection<Group>>(items) {}
    ;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param group
   *
   * @return
   */
  @Override
  protected String getId(Group group)
  {
    return group.getName();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected String getPathPart()
  {
    return PATH_PART;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Provider<WebSecurityContext> securityContextProvider;
}
