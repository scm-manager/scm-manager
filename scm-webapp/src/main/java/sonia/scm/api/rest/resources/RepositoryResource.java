/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.api.rest.resources;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.Repository;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.inject.Singleton;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
@Path("repositories")
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public class RepositoryResource
{

  /**
   * Constructs ...
   *
   */
  public RepositoryResource()
  {
    repositoryStore = new LinkedHashMap<String, Repository>();
    repositoryStore.put("sonia.lib",
                        new Repository("hg", "sonia.lib", "csit@ostfalia.de",
                                       "SONIA Library"));
    repositoryStore.put("sonia.misc",
                        new Repository("hg", "sonia.misc", "csit@ostfalia.de",
                                       "SONIA Miscelanious"));
    repositoryStore.put("PWA",
                        new Repository("svn", "PWA",
                                       "csit@fh-wolfenbuettel.de", "PWA"));
    repositoryStore.put("sonia.app",
                        new Repository("hg", "sonia.app", "csit@ostfalia.de",
                                       "SONIA Applications"));
    repositoryStore.put("sonia.webapps",
                        new Repository("hg", "sonia.webapps",
                                       "csit@ostfalia.de",
                                       "SONIA WebApplications"));
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param name
   *
   * @return
   */
  @GET
  @Path("{name}")
  public Repository get(@PathParam("name") String name)
  {
    Repository repository = repositoryStore.get(name);

    if (repository == null)
    {
      throw new WebApplicationException(Status.NOT_FOUND);
    }

    return repository;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @GET
  public Repository[] getAll()
  {
    Collection<Repository> repositoryCollection = repositoryStore.values();

    return repositoryCollection.toArray(
        new Repository[repositoryCollection.size()]);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private HashMap<String, Repository> repositoryStore;
}
