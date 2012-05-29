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
import org.codehaus.enunciate.modules.jersey.ExternallyManagedLifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.NotSupportedFeatuerException;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.BlameResult;
import sonia.scm.repository.BlameViewerUtil;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.ChangesetViewerUtil;
import sonia.scm.repository.DiffViewer;
import sonia.scm.repository.Permission;
import sonia.scm.repository.PermissionType;
import sonia.scm.repository.PermissionUtil;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryBrowser;
import sonia.scm.repository.RepositoryBrowserUtil;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.RepositoryHandler;
import sonia.scm.repository.RepositoryIsNotArchivedException;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryNotFoundException;
import sonia.scm.repository.RepositoryUtil;
import sonia.scm.security.ScmSecurityException;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.HttpUtil;
import sonia.scm.util.Util;
import sonia.scm.web.security.WebSecurityContext;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.ArrayList;
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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
@Path("repositories")
@ExternallyManagedLifecycle
public class RepositoryResource
        extends AbstractManagerResource<Repository, RepositoryException>
{

  /** Field description */
  public static final String PATH_PART = "repositories";

  /** the logger for RepositoryResource */
  private static final Logger logger =
    LoggerFactory.getLogger(RepositoryResource.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param configuration
   * @param repositoryManager
   * @param securityContextProvider
   * @param changesetViewerUtil
   * @param repositoryBrowserUtil
   * @param blameViewerUtil
   */
  @Inject
  public RepositoryResource(
          ScmConfiguration configuration, RepositoryManager repositoryManager,
          Provider<WebSecurityContext> securityContextProvider,
          ChangesetViewerUtil changesetViewerUtil,
          RepositoryBrowserUtil repositoryBrowserUtil,
          BlameViewerUtil blameViewerUtil)
  {
    super(repositoryManager);
    this.configuration = configuration;
    this.repositoryManager = repositoryManager;
    this.securityContextProvider = securityContextProvider;
    this.changesetViewerUtil = changesetViewerUtil;
    this.repositoryBrowserUtil = repositoryBrowserUtil;
    this.blameViewerUtil = blameViewerUtil;
    setDisableCache(false);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Creates a new repository.<br />
   * This method requires admin privileges.<br />
   * <br />
   * Status codes:
   * <ul>
   *   <li>201 create success</li>
   *   <li>403 forbidden, the current user has no admin privileges</li>
   *   <li>500 internal server error</li>
   * </ul>
   *
   * @param uriInfo current uri informations
   * @param repository the repository to be created
   *
   * @return
   */
  @POST
  @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  @Override
  public Response create(@Context UriInfo uriInfo, Repository repository)
  {
    return super.create(uriInfo, repository);
  }

  /**
   * Deletes a repository.<br />
   * This method requires owner privileges.<br />
   * <br />
   * Status codes:
   * <ul>
   *  <li>201 delete success</li>
   *  <li>403 forbidden, the current user has no owner privileges</li>
   *  <li>
   *    412 forbidden, the repository is not archived,
   *    this error occurs only with enabled repository archive.
   *  </li>
   *  <li>500 internal server error</li>
   * </ul>
   *
   * @param id the id of the repository to delete.
   *
   * @return
   */
  @DELETE
  @Path("{id}")
  @Override
  public Response delete(@PathParam("id") String id)
  {
    Response response = null;
    Repository repository = manager.get(id);

    if (repository != null)
    {
      preDelete(repository);

      try
      {
        manager.delete(repository);
        response = Response.noContent().build();
      }
      catch (RepositoryIsNotArchivedException ex)
      {
        logger.warn("non archived repository could not be deleted", ex);
        response = Response.status(Response.Status.PRECONDITION_FAILED).build();
      }
      catch (ScmSecurityException ex)
      {
        logger.warn("delete not allowd", ex);
        response = Response.status(Response.Status.FORBIDDEN).build();
      }
      catch (Exception ex)
      {
        logger.error("error during create", ex);
        response = createErrorResonse(ex);
      }
    }
    else
    {
      logger.warn("could not find repository {}", id);
      response = Response.status(Status.NOT_FOUND).build();
    }

    return response;
  }

  /**
   * Modifies the given repository.<br />
   * This method requires owner privileges.<br />
   * <br />
   * Status codes:
   * <ul>
   *   <li>201 update successful</li>
   *   <li>403 forbidden, the current user has no owner privileges</li>
   *   <li>500 internal server error</li>
   * </ul>
   *
   * @param uriInfo current uri informations
   * @param id id of the repository to be modified
   * @param repository repository object to modify
   *
   * @return
   */
  @PUT
  @Path("{id}")
  @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  @Override
  public Response update(@Context UriInfo uriInfo, @PathParam("id") String id,
                         Repository repository)
  {
    return super.update(uriInfo, id, repository);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the {@link Repository} with the specified id.<br />
   * <br />
   * Status codes:
   * <ul>
   *   <li>200 get successful</li>
   *   <li>404 not found, no repository with the specified id available</li>
   *   <li>500 internal server error</li>
   * </ul>
   *
   * @param request the current request
   * @param id the id/name of the user
   *
   * @return the {@link Repository} with the specified id
   */
  @GET
  @Path("{id}")
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  @TypeHint(Repository.class)
  @Override
  public Response get(@Context Request request, @PathParam("id") String id)
  {
    return super.get(request, id);
  }

  /**
   * Returns all repositories.<br />
   * <br />
   * Status codes:
   * <ul>
   *   <li>200 get successful</li>
   *   <li>500 internal server error</li>
   * </ul>
   *
   * @param request the current request
   * @param start the start value for paging
   * @param limit the limit value for paging
   * @param sortby sort parameter
   * @param desc sort direction desc or aesc
   *
   * @return all repositories
   */
  @GET
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  @TypeHint(Repository[].class)
  @Override
  public Response getAll(@Context Request request, @DefaultValue("0")
  @QueryParam("start") int start, @DefaultValue("-1")
  @QueryParam("limit") int limit, @QueryParam("sortby") String sortby,
                                  @DefaultValue("false")
  @QueryParam("desc") boolean desc)
  {
    return super.getAll(request, start, limit, sortby, desc);
  }

  /**
   * Returns a annotate/blame view for the given path.<br />
   * <br />
   * Status codes:
   * <ul>
   *   <li>200 get successful</li>
   *   <li>400 bad request, the blame feature is not
   *       supported by this type of repositories.</li>
   *   <li>404 not found, if the repository or the path could not be found</li>
   *   <li>500 internal server error</li>
   * </ul>
   *
   * @param id the id of the repository
   * @param revision the revision of the file
   * @param path the path of the file
   *
   * @return a annotate/blame view for the given path
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @GET
  @Path("{id}/blame")
  @TypeHint(BlameResult.class)
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public Response getBlame(@PathParam("id") String id,
                           @QueryParam("revision") String revision,
                           @QueryParam("path") String path)
          throws RepositoryException, IOException
  {
    Response response = null;

    try
    {
      AssertUtil.assertIsNotNull(path);

      BlameResult blamePagingResult = blameViewerUtil.getBlame(id, revision,
                                        path);

      if (blamePagingResult != null)
      {
        response = Response.ok(blamePagingResult).build();
      }
      else
      {
        response = Response.ok().build();
      }
    }
    catch (IllegalStateException ex)
    {
      response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    catch (RepositoryNotFoundException ex)
    {
      response = Response.status(Response.Status.NOT_FOUND).build();
    }
    catch (NotSupportedFeatuerException ex)
    {
      response = Response.status(Response.Status.BAD_REQUEST).build();
    }

    return response;
  }

  /**
   * Returns a list of folders and files for the given folder.<br />
   * <br />
   * Status codes:
   * <ul>
   *   <li>200 get successful</li>
   *   <li>400 bad request, the browse feature is not
   *       supported by this type of repositories.</li>
   *   <li>404 not found, if the repository or the path could not be found</li>
   *   <li>500 internal server error</li>
   * </ul>
   *
   * @param id the id of the repository
   * @param revision the revision of the file
   * @param path the path of the folder
   *
   * @return a list of folders and files for the given folder
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @GET
  @Path("{id}/browse")
  @TypeHint(BrowserResult.class)
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public Response getBrowserResult(@PathParam("id") String id,
                                   @QueryParam("revision") String revision,
                                   @QueryParam("path") String path)
          throws RepositoryException, IOException
  {
    Response response = null;

    try
    {
      BrowserResult result = repositoryBrowserUtil.getResult(id, revision,
                               path);

      if (result != null)
      {
        response = Response.ok(result).build();
      }
      else
      {
        response = Response.status(Response.Status.NOT_FOUND).build();
      }
    }
    catch (RepositoryNotFoundException ex)
    {
      response = Response.status(Response.Status.NOT_FOUND).build();
    }
    catch (NotSupportedFeatuerException ex)
    {
      response = Response.status(Response.Status.BAD_REQUEST).build();
    }

    return response;
  }

  /**
   * Returns the {@link Repository} with the specified type and name.<br />
   * <br />
   * Status codes:
   * <ul>
   *   <li>200 get successful</li>
   *   <li>404 not found,
   *       no repository with the specified type and name available</li>
   *   <li>500 internal server error</li>
   * </ul>
   *
   * @param type the type of the repository
   * @param name the name of the repository
   *
   * @return the {@link Repository} with the specified type and name
   */
  @GET
  @Path("{type: [a-z]+}/{name: .*}")
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  @TypeHint(Repository.class)
  public Response getByTypeAndName(@PathParam("type") String type,
                                   @PathParam("name") String name)
  {
    Response response = null;
    Repository repository = repositoryManager.get(type, name);

    if (repository != null)
    {
      prepareForReturn(repository);
      response = Response.ok(repository).build();
    }
    else
    {
      response = Response.status(Response.Status.NOT_FOUND).build();
    }

    return response;
  }

  /**
   * Returns the {@link Changeset} from the given repository
   * with the specified revision.<br />
   * <br />
   * Status codes:
   * <ul>
   *   <li>200 get successful</li>
   *   <li>400 bad request, the changeset feature is not
   *       supported by this type of repositories.</li>
   *   <li>404 not found, if the repository or
   *       the revision could not be found</li>
   *   <li>500 internal server error</li>
   * </ul>
   *
   * @param id the id of the repository
   * @param revision the revision of the changeset
   *
   * @return a {@link Changeset} from the given repository
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @GET
  @Path("{id}/changeset/{revision}")
  public Response getChangeset(@PathParam("id") String id,
                               @PathParam("revision") String revision)
          throws IOException, RepositoryException
  {
    Response response = null;

    if (Util.isNotEmpty(id) && Util.isNotEmpty(revision))
    {
      try
      {
        Changeset changeset = changesetViewerUtil.getChangeset(id, revision);

        if (changeset != null)
        {
          response = Response.ok(changeset).build();
        }
        else
        {
          response = Response.status(Status.NOT_FOUND).build();
        }
      }
      catch (RepositoryNotFoundException ex)
      {
        response = Response.status(Response.Status.NOT_FOUND).build();
      }
      catch (NotSupportedFeatuerException ex)
      {
        response = Response.status(Response.Status.BAD_REQUEST).build();
      }
    }
    else
    {
      if (logger.isWarnEnabled())
      {
        logger.warn("id or revision is empty");
      }

      response = Response.status(Status.BAD_REQUEST).build();
    }

    return response;
  }

  /**
   * Returns a list of {@link Changeset} for the given repository.<br />
   * <br />
   * Status codes:
   * <ul>
   *   <li>200 get successful</li>
   *   <li>400 bad request, the changeset feature is not
   *       supported by this type of repositories.</li>
   *   <li>404 not found, if the repository or the path could not be found</li>
   *   <li>500 internal server error</li>
   * </ul>
   *
   * @param id the id of the repository
   * @param path path of a file
   * @param revision the revision of the file specified by the path parameter
   * @param start the start value for paging
   * @param limit the limit value for paging
   *
   * @return a list of {@link Changeset} for the given repository
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @GET
  @Path("{id}/changesets")
  @TypeHint(ChangesetPagingResult.class)
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public Response getChangesets(@PathParam("id") String id,
                                @QueryParam("path") String path,
                                @QueryParam("revision") String revision,
                                @DefaultValue("0")
  @QueryParam("start") int start, @DefaultValue("20")
  @QueryParam("limit") int limit) throws RepositoryException, IOException
  {
    Response response = null;

    try
    {
      ChangesetPagingResult changesets = null;

      if (Util.isEmpty(path))
      {
        changesets = changesetViewerUtil.getChangesets(id, start, limit);
      }
      else
      {
        changesets = changesetViewerUtil.getChangesets(id, path, revision,
                start, limit);
      }

      if (changesets != null)
      {
        response = Response.ok(changesets).build();
      }
      else
      {
        response = Response.ok().build();
      }
    }
    catch (RepositoryNotFoundException ex)
    {
      response = Response.status(Response.Status.NOT_FOUND).build();
    }
    catch (NotSupportedFeatuerException ex)
    {
      response = Response.status(Response.Status.BAD_REQUEST).build();
    }

    return response;
  }

  /**
   * Returns the content of a file.<br />
   * <br />
   * Status codes:
   * <ul>
   *   <li>200 get successful</li>
   *   <li>400 bad request, the content feature is not
   *       supported by this type of repositories.</li>
   *   <li>404 not found, if the repository or the path could not be found</li>
   *   <li>500 internal server error</li>
   * </ul>
   *
   * @param id the id of the repository
   * @param revision the revision of the file
   * @param path path to the file
   *
   * @return the content of a file
   */
  @GET
  @Path("{id}/content")
  @TypeHint(StreamingOutput.class)
  @Produces({ MediaType.APPLICATION_OCTET_STREAM })
  public Response getContent(@PathParam("id") String id,
                             @QueryParam("revision") String revision,
                             @QueryParam("path") String path)
  {
    Response response = null;
    StreamingOutput output = null;
    Repository repository = repositoryManager.get(id);

    if (repository != null)
    {
      try
      {
        RepositoryBrowser browser =
          repositoryManager.getRepositoryBrowser(repository);

        if (browser != null)
        {
          output = new BrowserStreamingOutput(browser, revision, path);

          String contentDispositionName =
            getContentDispositionNameFromPath(path);

          response = Response.ok(output).header("Content-Disposition",
                                 contentDispositionName).build();
        }
        else if (logger.isWarnEnabled())
        {
          logger.warn("could not find repository browser for respository {}",
                      repository.getId());
          response = Response.status(Response.Status.NOT_FOUND).build();
        }
      }
      catch (Exception ex)
      {
        logger.error("could not retrive content", ex);
        response = createErrorResonse(ex);
      }
    }

    return response;
  }

  /**
   * Returns the modifications of a {@link Changeset}.<br />
   * <br />
   * Status codes:
   * <ul>
   *   <li>200 get successful</li>
   *   <li>400 bad request, the content feature is not
   *       supported by this type of repositories.</li>
   *   <li>404 not found, if the repository or the path could not be found</li>
   *   <li>500 internal server error</li>
   * </ul>
   *
   * @param id the id of the repository
   * @param revision the revision of the file
   * @param path path to the file
   *
   * @return the modifications of a {@link Changeset}
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @GET
  @Path("{id}/diff")
  @TypeHint(DiffStreamingOutput.class)
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response getDiff(@PathParam("id") String id,
                          @QueryParam("revision") String revision,
                          @QueryParam("path") String path)
          throws RepositoryException, IOException
  {
    AssertUtil.assertIsNotEmpty(id);
    AssertUtil.assertIsNotEmpty(revision);

    Response response = null;

    try
    {
      Repository repository = repositoryManager.get(id);

      if (repository != null)
      {
        DiffViewer diffViewer = repositoryManager.getDiffViewer(repository);

        if (diffViewer != null)
        {
          String name =
            repository.getName().concat("-").concat(revision).concat(".diff");
          String contentDispositionName = getContentDispositionName(name);

          response = Response.ok(new DiffStreamingOutput(diffViewer, revision,
                  path)).header("Content-Disposition",
                                contentDispositionName).build();
        }
        else
        {
          response = Response.status(Response.Status.NOT_FOUND).build();
        }
      }
    }
    catch (RepositoryNotFoundException ex)
    {
      response = Response.status(Response.Status.NOT_FOUND).build();
    }

    return response;
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
  protected GenericEntity<Collection<Repository>> createGenericEntity(
          Collection<Repository> items)
  {
    return new GenericEntity<Collection<Repository>>(items) {}
    ;
  }

  /**
   * Method description
   *
   *
   *
   * @param repositories
   * @return
   */
  @Override
  protected Collection<Repository> prepareForReturn(
          Collection<Repository> repositories)
  {
    for (Repository repository : repositories)
    {
      RepositoryUtil.appendUrl(configuration, repositoryManager, repository);
      prepareRepository(repository);
    }

    return repositories;
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
  @Override
  protected Repository prepareForReturn(Repository repository)
  {
    RepositoryUtil.appendUrl(configuration, repositoryManager, repository);
    prepareRepository(repository);

    return repository;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param item
   *
   * @return
   */
  @Override
  protected String getId(Repository item)
  {
    return item.getId();
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

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repository
   */
  private void prepareRepository(Repository repository)
  {
    if (isOwner(repository))
    {
      if (repository.getPermissions() == null)
      {
        repository.setPermissions(new ArrayList<Permission>());
      }
    }
    else
    {
      repository.setProperties(null);
      repository.setPermissions(null);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   * @param name
   *
   * @return
   */
  private String getContentDispositionName(String name)
  {
    return "attachment; filename=\"".concat(name).concat("\"");
  }

  /**
   * Method description
   *
   *
   * @param path
   *
   * @return
   */
  private String getContentDispositionNameFromPath(String path)
  {
    String name = path;
    int index = path.lastIndexOf("/");

    if (index >= 0)
    {
      name = path.substring(index + 1);
    }

    return getContentDispositionName(name);
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
  private boolean isOwner(Repository repository)
  {
    return PermissionUtil.hasPermission(repository, securityContextProvider,
            PermissionType.OWNER);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private BlameViewerUtil blameViewerUtil;

  /** Field description */
  private ChangesetViewerUtil changesetViewerUtil;

  /** Field description */
  private ScmConfiguration configuration;

  /** Field description */
  private RepositoryBrowserUtil repositoryBrowserUtil;

  /** Field description */
  private RepositoryManager repositoryManager;

  /** Field description */
  private Provider<WebSecurityContext> securityContextProvider;
}
