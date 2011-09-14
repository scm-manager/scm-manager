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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.NotSupportedFeatuerException;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.BlamePagingResult;
import sonia.scm.repository.BlameViewerUtil;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.ChangesetViewerUtil;
import sonia.scm.repository.PathNotFoundException;
import sonia.scm.repository.Permission;
import sonia.scm.repository.PermissionType;
import sonia.scm.repository.PermissionUtil;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryBrowser;
import sonia.scm.repository.RepositoryBrowserUtil;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.RepositoryHandler;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryNotFoundException;
import sonia.scm.repository.RevisionNotFoundException;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.HttpUtil;
import sonia.scm.web.security.WebSecurityContext;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.Collection;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
@Path("repositories")
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

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param id
   * @param revision
   * @param path
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @GET
  @Path("{id}/browse")
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
   * Method description
   *
   *
   * @param id
   * @param start
   * @param limit
   *
   * @return
   *
   * @throws RepositoryException
   */
  @GET
  @Path("{id}/changesets")
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public Response getChangesets(@PathParam("id") String id, @DefaultValue("0")
  @QueryParam("start") int start, @DefaultValue("20")
  @QueryParam("limit") int limit) throws RepositoryException
  {
    Response response = null;

    try
    {
      ChangesetPagingResult changesets = changesetViewerUtil.getChangesets(id,
                                           start, limit);

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
  
  @GET
  @Path("{id}/blame")
  public Response blame(@PathParam("id") String id,
  			@QueryParam("revision") String revision,
  			@QueryParam("path") String path)
  {
  	Response response = null;
  	
    try {
    	AssertUtil.assertIsNotNull(path);
	  	BlamePagingResult blamePagingResult = blameViewerUtil.getBlame(id, revision, path);
	  	
	  	if (blamePagingResult != null) {
	  		response = Response.ok(blamePagingResult).build();
	  	}
	  	else 
	  	{
	  		response = Response.ok().build();
	  	}
    } catch (IllegalStateException ex) {
    	response = Response.status(Response.Status.NOT_FOUND).build();
    } catch (RepositoryException ex) {
      response = Response.status(Response.Status.NOT_FOUND).build();
    } catch (NotSupportedFeatuerException ex) {
      response = Response.status(Response.Status.BAD_REQUEST).build();
    }
    
    return response;
  }


  /**
   * Method description
   *
   *
   * @param id
   * @param revision
   * @param path
   *
   * @return
   */
  @GET
  @Path("{id}/content")
  @Produces({ MediaType.APPLICATION_OCTET_STREAM })
  public StreamingOutput getContent(@PathParam("id") String id,
                                    @QueryParam("revision") String revision,
                                    @QueryParam("path") String path)
  {
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
        }
        else if (logger.isWarnEnabled())
        {
          logger.warn("could not find repository browser for respository {}",
                      repository.getId());
        }
      }
      catch (Exception ex)
      {
        logger.error("could not retrive content", ex);
      }
    }

    return output;
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
      appendUrl(repository);
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
    appendUrl(repository);
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
  private void appendUrl(Repository repository)
  {
    RepositoryHandler handler =
      repositoryManager.getHandler(repository.getType());

    if (handler != null)
    {
      String url = handler.createResourcePath(repository);

      url = HttpUtil.getCompleteUrl(configuration, url);
      repository.setUrl(url);
    }
  }

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
   * @param repository
   *
   * @return
   */
  private boolean isOwner(Repository repository)
  {
    return PermissionUtil.hasPermission(repository, securityContextProvider,
            PermissionType.OWNER);
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 11/06/18
   * @author         Enter your name here...
   */
  private static class BrowserStreamingOutput implements StreamingOutput
  {

    /**
     * Constructs ...
     *
     *
     * @param browser
     * @param revision
     * @param path
     */
    public BrowserStreamingOutput(RepositoryBrowser browser, String revision,
                                  String path)
    {
      this.browser = browser;
      this.revision = revision;
      this.path = path;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param output
     *
     * @throws IOException
     * @throws WebApplicationException
     */
    @Override
    public void write(OutputStream output)
            throws IOException, WebApplicationException
    {
      try
      {
        browser.getContent(revision, path, output);
      }
      catch (PathNotFoundException ex)
      {
        if (logger.isWarnEnabled())
        {
          logger.warn("could not find path {}", ex.getPath());
        }

        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
      catch (RevisionNotFoundException ex)
      {
        if (logger.isWarnEnabled())
        {
          logger.warn("could not find revision {}", ex.getRevision());
        }

        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
      catch (RepositoryException ex)
      {
        logger.error("could not write content to page", ex);

        throw new WebApplicationException(
            ex, Response.Status.INTERNAL_SERVER_ERROR);
      }
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private RepositoryBrowser browser;

    /** Field description */
    private String path;

    /** Field description */
    private String revision;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private ChangesetViewerUtil changesetViewerUtil;

  /** Field description */
  private ScmConfiguration configuration;

  /** Field description */
  private RepositoryBrowserUtil repositoryBrowserUtil;
  
  /** Field description */
  private BlameViewerUtil blameViewerUtil;

  /** Field description */
  private RepositoryManager repositoryManager;
  
  /** Field description */
  private Provider<WebSecurityContext> securityContextProvider;
}
