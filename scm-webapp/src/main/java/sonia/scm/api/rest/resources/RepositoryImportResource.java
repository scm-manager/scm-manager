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

import org.codehaus.enunciate.jaxrs.TypeHint;
import org.codehaus.enunciate.modules.jersey.ExternallyManagedLifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.NotSupportedFeatuerException;
import sonia.scm.Type;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.RepositoryHandler;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.util.SecurityUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Rest resource for importing repositories.
 *
 * @author Sebastian Sdorra
 */
@Path("import/repositories")
@ExternallyManagedLifecycle
public class RepositoryImportResource
{

  /**
   * the logger for RepositoryImportResource
   */
  private static final Logger logger =
    LoggerFactory.getLogger(RepositoryImportResource.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new repository import resource.
   *
   * @param manager repository manager
   */
  @Inject
  public RepositoryImportResource(RepositoryManager manager)
  {
    this.manager = manager;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Imports repositories of the given type from the configured repository
   * directory. This method requires admin privileges.<br />
   * <br />
   * Status codes:
   * <ul>
   *   <li>200 ok, successful</li>
   *   <li>400 bad request, the import feature is not
   *       supported by this type of repositories.</li>
   *   <li>500 internal server error</li>
   * </ul>
   *
   * @param type repository type
   *
   * @return imported repositories
   */
  @POST
  @Path("{type}")
  @TypeHint(Repository[].class)
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public Response importRepositories(@PathParam("type") String type)
  {
    SecurityUtil.assertIsAdmin();

    List<Repository> repositories = new ArrayList<Repository>();
    RepositoryHandler handler = manager.getHandler(type);

    if (handler != null)
    {
      try
      {
        List<String> repositoryNames =
          handler.getImportHandler().importRepositories(manager);

        if (repositoryNames != null)
        {
          for (String repositoryName : repositoryNames)
          {
            Repository repository = manager.get(type, repositoryName);

            if (repository != null)
            {
              repositories.add(repository);
            }
            else if (logger.isWarnEnabled())
            {
              logger.warn("could not find imported repository {}",
                repositoryName);
            }
          }
        }
      }
      catch (NotSupportedFeatuerException ex)
      {
        throw new WebApplicationException(ex, Response.Status.BAD_REQUEST);
      }
      catch (IOException ex)
      {
        throw new WebApplicationException(ex);
      }
      catch (RepositoryException ex)
      {
        throw new WebApplicationException(ex);
      }
    }
    else if (logger.isWarnEnabled())
    {
      logger.warn("could not find handler for type {}", type);
    }

    //J-
    return Response.ok(
      new GenericEntity<List<Repository>>(repositories) {}
    ).build();
    //J+
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns a list of repository types, which support the import feature.
   * This method requires admin privileges.<br />
   * <br />
   * Status codes:
   * <ul>
   *   <li>200 ok, successful</li>
   *   <li>400 bad request, the import feature is not
   *       supported by this type of repositories.</li>
   *   <li>500 internal server error</li>
   * </ul>
   *
   * @return list of repository types
   */
  @GET
  @TypeHint(Type[].class)
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public Response getImportableTypes()
  {
    SecurityUtil.assertIsAdmin();

    List<Type> types = new ArrayList<Type>();
    Collection<Type> handlerTypes = manager.getTypes();

    for (Type t : handlerTypes)
    {
      RepositoryHandler handler = manager.getHandler(t.getName());

      if (handler != null)
      {
        try
        {
          if (handler.getImportHandler() != null)
          {
            types.add(t);
          }
        }
        catch (NotSupportedFeatuerException ex)
        {
          if (logger.isTraceEnabled())
          {
            logger.trace("import handler is not supported", ex);
          }
          else if (logger.isInfoEnabled())
          {
            logger.info("{} handler does not support import of repositories",
              t.getName());
          }
        }
      }
      else if (logger.isWarnEnabled())
      {
        logger.warn("could not find handler for type {}", t.getName());
      }
    }

    //J-
    return Response.ok(
      new GenericEntity<List<Type>>(types) {}
    ).build();
    //J+
  }

  //~--- fields ---------------------------------------------------------------

  /** repository manager */
  private final RepositoryManager manager;
}
