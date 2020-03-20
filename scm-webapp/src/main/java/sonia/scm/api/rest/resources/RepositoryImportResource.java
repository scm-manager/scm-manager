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
    
package sonia.scm.api.rest.resources;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.FeatureNotSupportedException;
import sonia.scm.NotFoundException;
import sonia.scm.Type;
import sonia.scm.api.rest.RestActionUploadResult;
import sonia.scm.api.v2.resources.RepositoryResource;
import sonia.scm.repository.AdvancedImportHandler;
import sonia.scm.repository.ImportHandler;
import sonia.scm.repository.ImportResult;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryHandler;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.RepositoryType;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.api.UnbundleCommandBuilder;
import sonia.scm.util.IOUtil;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Rest resource for importing repositories.
 *
 * @author Sebastian Sdorra
 */
// @Path("import/repositories")
public class RepositoryImportResource {

  /**
   * the logger for RepositoryImportResource
   */
  private static final Logger logger =
    LoggerFactory.getLogger(RepositoryImportResource.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new repository import resource.
   *
   * @param manager        repository manager
   * @param serviceFactory
   */
  @Inject
  public RepositoryImportResource(RepositoryManager manager,
                                  RepositoryServiceFactory serviceFactory) {
    this.manager = manager;
    this.serviceFactory = serviceFactory;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Imports a repository type specific bundle. The bundle file is uploaded to
   * the server which is running scm-manager. After the upload has finished, the
   * bundle file is passed to the {@link UnbundleCommandBuilder}. <strong>Note:</strong> This method
   * requires admin privileges.
   *
   * @param uriInfo     uri info
   * @param type        repository type
   * @param name        name of the repository
   * @param inputStream input bundle
   * @param compressed  true if the bundle is gzip compressed
   * @return empty response with location header which points to the imported repository
   * @since 1.43
   */
  @POST
  @Path("{type}/bundle")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Response importFromBundle(@Context UriInfo uriInfo,
                                   @PathParam("type") String type, @FormParam("name") String name,
                                   @FormParam("bundle") InputStream inputStream, @QueryParam("compressed")
                                   @DefaultValue("false") boolean compressed) {
    Repository repository = doImportFromBundle(type, name, inputStream,
      compressed);

    return buildResponse(uriInfo, repository);
  }

  /**
   * This method works exactly like
   * {@link #importFromBundle(UriInfo, String, String, InputStream)}, but this
   * method returns an html content-type. The method exists only for a
   * workaround of the javascript ui extjs. <strong>Note:</strong> This method requires admin
   * privileges.
   *
   * @param type        repository type
   * @param name        name of the repository
   * @param inputStream input bundle
   * @param compressed  true if the bundle is gzip compressed
   * @return empty response with location header which points to the imported
   * repository
   * @since 1.43
   */
  @POST
  @Path("{type}/bundle.html")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.TEXT_HTML)
  public Response importFromBundleUI(@PathParam("type") String type,
                                     @FormParam("name") String name,
                                     @FormParam("bundle") InputStream inputStream, @QueryParam("compressed")
                                     @DefaultValue("false") boolean compressed) {
    Response response;

    try {
      doImportFromBundle(type, name, inputStream, compressed);
      response = Response.ok(new RestActionUploadResult(true)).build();
    } catch (WebApplicationException ex) {
      logger.warn("error durring bundle import", ex);
      response = Response.fromResponse(ex.getResponse()).entity(
        new RestActionUploadResult(false)).build();
    }

    return response;
  }

  /**
   * Imports a external repository which is accessible via url. The method can
   * only be used, if the repository type supports the {@link Command#PULL}. The
   * method will return a location header with the url to the imported
   * repository. <strong>Note:</strong> This method requires admin privileges.
   *
   * @param uriInfo uri info
   * @param type    repository type
   * @param request request object
   * @return empty response with location header which points to the imported
   * repository
   * @since 1.43
   */
  @POST
  @Path("{type}/url")
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Response importFromUrl(@Context UriInfo uriInfo,
                                @PathParam("type") String type, UrlImportRequest request) {
    RepositoryPermissions.create().check();
    checkNotNull(request, "request is required");
    checkArgument(!Strings.isNullOrEmpty(request.getName()),
      "request does not contain name of the repository");
    checkArgument(!Strings.isNullOrEmpty(request.getUrl()),
      "request does not contain url of the remote repository");

    Type t = type(type);

    checkSupport(t, Command.PULL, request);

    logger.info("start {} import for external url {}", type, request.getUrl());

    Repository repository = create(type, request.getName());
    RepositoryService service = null;

    try {
      service = serviceFactory.create(repository);
      service.getPullCommand().pull(request.getUrl());
    } catch (IOException ex) {
      handleImportFailure(ex, repository);
    } finally {
      IOUtil.close(service);
    }

    return buildResponse(uriInfo, repository);
  }

  /**
   * Imports repositories of the given type from the configured repository
   * directory. <strong>Note:</strong> This method requires admin privileges.
   *
   * @param type repository type
   * @return imported repositories
   */
  @POST
  @Path("{type}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Response importRepositories(@PathParam("type") String type) {
    RepositoryPermissions.create().check();

    List<Repository> repositories = new ArrayList<Repository>();

    importFromDirectory(repositories, type);

    //J-
    return Response.ok(
      new GenericEntity<List<Repository>>(repositories) {
      }
    ).build();
    //J+
  }

  /**
   * Imports repositories of all supported types from the configured repository
   * directories. <strong>Note:</strong> This method requires admin privileges.
   *
   * @return imported repositories
   */
  @POST
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Response importRepositories() {
    RepositoryPermissions.create().check();

    logger.info("start directory import for all supported repository types");

    List<Repository> repositories = new ArrayList<Repository>();

    for (Type t : findImportableTypes()) {
      importFromDirectory(repositories, t.getName());
    }

    //J-
    return Response.ok(
      new GenericEntity<List<Repository>>(repositories) {
      }
    ).build();
    //J+
  }

  /**
   * Imports repositories of the given type from the configured repository
   * directory. Returns a list of successfully imported directories and a list
   * of failed directories. <strong>Note:</strong> This method requires admin privileges.
   *
   * @param type repository type
   * @return imported repositories
   * @since 1.43
   */
  @POST
  @Path("{type}/directory")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Response importRepositoriesFromDirectory(
    @PathParam("type") String type) {
    RepositoryPermissions.create().check();

    Response response;

    RepositoryHandler handler = manager.getHandler(type);

    if (handler != null) {
      logger.info("start directory import for repository type {}", type);

      try {
        ImportResult result;
        ImportHandler importHandler = handler.getImportHandler();

        if (importHandler instanceof AdvancedImportHandler) {
          logger.debug("start directory import, using advanced import handler");
          result =
            ((AdvancedImportHandler) importHandler)
              .importRepositoriesFromDirectory(manager);
        } else {
          logger.debug("start directory import, using normal import handler");
          result = new ImportResult(importHandler.importRepositories(manager),
            ImmutableList.<String>of());
        }

        response = Response.ok(result).build();
      } catch (FeatureNotSupportedException ex) {
        logger
          .warn(
            "import feature is not supported by repository handler for type "
              .concat(type), ex);
        response = Response.status(Response.Status.BAD_REQUEST).build();
      } catch (IOException ex) {
        logger.warn("exception occured durring directory import", ex);
        response = Response.serverError().build();
      }
    } else {
      logger.warn("could not find reposiotry handler for type {}", type);
      response = Response.status(Response.Status.BAD_REQUEST).build();
    }

    return response;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns a list of repository types, which support the directory import
   * feature. <strong>Note:</strong> This method requires admin privileges.
   *
   * @return list of repository types
   */
  @GET
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Response getImportableTypes() {
    RepositoryPermissions.create().check();

    List<Type> types = findImportableTypes();

    //J-
    return Response.ok(
      new GenericEntity<List<Type>>(types) {
      }
    ).build();
    //J+
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Build rest response for repository.
   *
   * @param uriInfo    uri info
   * @param repository imported repository
   * @return rest response
   */
  private Response buildResponse(UriInfo uriInfo, Repository repository) {
    URI location = uriInfo.getBaseUriBuilder().path(
      RepositoryResource.class).path(repository.getId()).build();

    return Response.created(location).build();
  }

  /**
   * Check repository type for support for the given command.
   *
   * @param type    repository type
   * @param cmd     command
   * @param request request object
   */
  private void checkSupport(Type type, Command cmd, Object request) {
    if (!(type instanceof RepositoryType)) {
      logger.warn("type {} is not a repository type", type.getName());

      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    Set<Command> cmds = ((RepositoryType) type).getSupportedCommands();

    if (!cmds.contains(cmd)) {
      logger.warn("type {} does not support this type of import: {}",
        type.getName(), request);

      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
  }

  /**
   * Creates a new repository with the given name and type.
   *
   * @param type repository type
   * @param name repository name
   * @return newly created repository
   */
  private Repository create(String type, String name) {
    Repository repository = null;

    try {
      // TODO #8783
//      repository = new Repository(null, type, name);
      manager.create(repository);
    } catch (InternalRepositoryException ex) {
      handleGenericCreationFailure(ex, type, name);
    }

    return repository;
  }

  /**
   * Start bundle import.
   *
   * @param type        repository type
   * @param name        name of the repository
   * @param inputStream bundle stream
   * @param compressed  true if the bundle is gzip compressed
   * @return imported repository
   */
  private Repository doImportFromBundle(String type, String name,
                                        InputStream inputStream, boolean compressed) {
    RepositoryPermissions.create().check();

    checkArgument(!Strings.isNullOrEmpty(name),
      "request does not contain name of the repository");
    checkNotNull(inputStream, "bundle inputStream is required");

    Repository repository;

    try {
      Type t = type(type);

      checkSupport(t, Command.UNBUNDLE, "bundle");

      repository = create(type, name);

      RepositoryService service = null;

      File file = File.createTempFile("scm-import-", ".bundle");

      try {
        long length = Files.asByteSink(file).writeFrom(inputStream);

        logger.info("copied {} bytes to temp, start bundle import", length);
        service = serviceFactory.create(repository);
        service.getUnbundleCommand().setCompressed(compressed).unbundle(file);
      } catch (InternalRepositoryException ex) {
        handleImportFailure(ex, repository);
      } finally {
        IOUtil.close(service);
        IOUtil.delete(file);
      }
    } catch (IOException ex) {
      logger.warn("could not create temporary file", ex);

      throw new WebApplicationException(ex);
    }

    return repository;
  }

  /**
   * Method description
   *
   * @return
   */
  private List<Type> findImportableTypes() {
    List<Type> types = new ArrayList<Type>();
    Collection<Type> handlerTypes = manager.getTypes();

    for (Type t : handlerTypes) {
      RepositoryHandler handler = manager.getHandler(t.getName());

      if (handler != null) {
        try {
          if (handler.getImportHandler() != null) {
            types.add(t);
          }
        } catch (FeatureNotSupportedException ex) {
          if (logger.isTraceEnabled()) {
            logger.trace("import handler is not supported", ex);
          } else if (logger.isInfoEnabled()) {
            logger.info("{} handler does not support import of repositories",
              t.getName());
          }
        }
      } else if (logger.isWarnEnabled()) {
        logger.warn("could not find handler for type {}", t.getName());
      }
    }

    return types;
  }

  /**
   * Handle creation failures.
   *
   * @param ex   exception
   * @param type repository type
   * @param name name of the repository
   */
  private void handleGenericCreationFailure(Exception ex, String type,
                                            String name) {
    logger.error(String.format("could not create repository %s with type %s",
      type, name), ex);

    throw new WebApplicationException(ex);
  }

  /**
   * Handle import failures.
   *
   * @param ex         exception
   * @param repository repository
   */
  private void handleImportFailure(Exception ex, Repository repository) {
    logger.error("import for repository failed, delete repository", ex);

    try {
      manager.delete(repository);
    } catch (InternalRepositoryException | NotFoundException e) {
      logger.error("can not delete repository after import failure", e);
    }

    throw new WebApplicationException(ex,
      Response.Status.INTERNAL_SERVER_ERROR);
  }

  /**
   * Import repositories from a specific type.
   *
   * @param repositories repository list
   * @param type         type of repository
   */
  private void importFromDirectory(List<Repository> repositories, String type) {
    RepositoryHandler handler = manager.getHandler(type);

    if (handler != null) {
      logger.info("start directory import for repository type {}", type);

      try {
        List<String> repositoryNames =
          handler.getImportHandler().importRepositories(manager);

        if (repositoryNames != null) {
          for (String repositoryName : repositoryNames) {
            // TODO #8783
            /*Repository repository = null; //manager.get(type, repositoryName);

            if (repository != null)
            {
              repositories.add(repository);
            }
            else if (logger.isWarnEnabled())
            {
              logger.warn("could not find imported repository {}",
                repositoryName);
            }*/
          }
        }
      } catch (FeatureNotSupportedException ex) {
        throw new WebApplicationException(ex, Response.Status.BAD_REQUEST);
      } catch (IOException ex) {
        throw new WebApplicationException(ex);
      } catch (InternalRepositoryException ex) {
        throw new WebApplicationException(ex);
      }
    } else {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
  }

  /**
   * Method description
   *
   * @param type
   * @return
   */
  private Type type(String type) {
    RepositoryHandler handler = manager.getHandler(type);

    if (handler == null) {
      logger.warn("no handler for type {} found", type);

      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    return handler.getType();
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Request for importing external repositories which are accessible via url.
   */
  @XmlRootElement(name = "import")
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class UrlImportRequest {

    /**
     * Constructs ...
     */
    public UrlImportRequest() {
    }

    /**
     * Constructs a new {@link UrlImportRequest}
     *
     * @param name name of the repository
     * @param url  external url of the repository
     */
    public UrlImportRequest(String name, String url) {
      this.name = name;
      this.url = url;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
      //J-
      return MoreObjects.toStringHelper(this)
        .add("name", name)
        .add("url", url)
        .toString();
      //J+
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Returns name of the repository.
     *
     * @return name of the repository
     */
    public String getName() {
      return name;
    }

    /**
     * Returns external url of the repository.
     *
     * @return external url of the repository
     */
    public String getUrl() {
      return url;
    }

    //~--- fields -------------------------------------------------------------

    /**
     * name of the repository
     */
    private String name;

    /**
     * external url of the repository
     */
    private String url;
  }


  //~--- fields ---------------------------------------------------------------

  /**
   * repository manager
   */
  private final RepositoryManager manager;

  /**
   * repository service factory
   */
  private final RepositoryServiceFactory serviceFactory;
}
