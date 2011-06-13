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

import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.ChangesetPreProcessor;
import sonia.scm.repository.ChangesetViewer;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.FileObjectNameComparator;
import sonia.scm.repository.Permission;
import sonia.scm.repository.PermissionType;
import sonia.scm.repository.PermissionUtil;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryBrowser;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.RepositoryHandler;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.util.HttpUtil;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;
import sonia.scm.web.security.WebSecurityContext;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

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
   * @param requestProvider
   * @param changesetPreProcessorSet
   */
  @Inject
  public RepositoryResource(
          ScmConfiguration configuration, RepositoryManager repositoryManager,
          Provider<WebSecurityContext> securityContextProvider,
          Provider<HttpServletRequest> requestProvider,
          Set<ChangesetPreProcessor> changesetPreProcessorSet)
  {
    super(repositoryManager);
    this.configuration = configuration;
    this.repositoryManager = repositoryManager;
    this.securityContextProvider = securityContextProvider;
    this.requestProvider = requestProvider;
    this.changesetPreProcessorSet = changesetPreProcessorSet;
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
   */
  @GET
  @Path("{id}/browse")
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public Response getBrowserResult(@PathParam("id") String id,
                                   @QueryParam("revision") String revision,
                                   @QueryParam("path") String path)
  {
    Response response = null;
    Repository repository = repositoryManager.get(id);

    if (repository != null)
    {
      BrowserResult result = null;

      try
      {
        RepositoryBrowser browser =
          repositoryManager.getRepositoryBrowser(repository);

        if (browser != null)
        {
          result = browser.getResult(revision, path);
          sort(result);
        }
        else if (logger.isWarnEnabled())
        {
          logger.warn("could not find repository browser for respository {}",
                      repository.getId());
        }
      }
      catch (Exception ex)
      {
        logger.error("could not retrive browserresult", ex);
      }

      if (result != null)
      {
        response = Response.ok(result).build();
      }
      else
      {
        response = Response.status(Response.Status.NOT_FOUND).build();
      }
    }
    else
    {
      response = Response.status(Response.Status.NOT_FOUND).build();
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
    Repository repository = repositoryManager.get(id);

    if (repository != null)
    {
      ChangesetViewer changesetViewer =
        repositoryManager.getChangesetViewer(repository);

      if (changesetViewer != null)
      {
        ChangesetPagingResult changesets = changesetViewer.getChangesets(start,
                                             limit);

        callPreProcessors(changesets);
        response = Response.ok(changesets).build();
      }
      else
      {
        response = Response.status(Response.Status.NOT_FOUND).build();
      }
    }
    else
    {
      response = Response.status(Response.Status.NOT_FOUND).build();
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
          final InputStream content = browser.getContent(revision, path);

          if (content != null)
          {
            output = new StreamingOutput()
            {
              @Override
              public void write(OutputStream output)
                      throws IOException, WebApplicationException
              {
                try
                {
                  IOUtil.copy(content, output);
                }
                finally
                {
                  IOUtil.close(content);
                }
              }
            };
          }
          else if (logger.isWarnEnabled())
          {
            logger.warn(
                "could not find content, repository: {}, path: {}, revision: {}",
                new Object[] { repository.getId(),
                               path, revision });
          }
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
      HttpServletRequest request = requestProvider.get();
      StringBuilder url = new StringBuilder(request.getScheme());

      url.append("://").append(configuration.getServername());
      url.append(":").append(HttpUtil.getServerPort(configuration, request));

      String ctxPath = request.getContextPath();

      if (ctxPath.endsWith("/"))
      {
        ctxPath = ctxPath.substring(0, ctxPath.length() - 1);
      }

      url.append(ctxPath);
      url.append(handler.createResourcePath(repository));
      repository.setUrl(url.toString());
    }
  }

  /**
   * Method description
   *
   *
   * @param changesets
   */
  private void callPreProcessors(ChangesetPagingResult changesets)
  {
    if (Util.isNotEmpty(changesetPreProcessorSet)
        && Util.isNotEmpty(changesets.getChangesets()))
    {
      for (Changeset c : changesets.getChangesets())
      {
        for (ChangesetPreProcessor cpp : changesetPreProcessorSet)
        {
          cpp.process(c);
        }
      }
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
      repository.setPermissions(null);
    }
  }

  /**
   * Method description
   *
   *
   * @param result
   */
  private void sort(BrowserResult result)
  {
    FileObject file = result.getFile();

    if (file != null)
    {
      List<FileObject> children = file.getChildren();

      if (children != null)
      {
        Collections.sort(children, FileObjectNameComparator.instance);
      }
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

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Set<ChangesetPreProcessor> changesetPreProcessorSet;

  /** Field description */
  private ScmConfiguration configuration;

  /** Field description */
  private RepositoryManager repositoryManager;

  /** Field description */
  private Provider<HttpServletRequest> requestProvider;

  /** Field description */
  private Provider<WebSecurityContext> securityContextProvider;
}
