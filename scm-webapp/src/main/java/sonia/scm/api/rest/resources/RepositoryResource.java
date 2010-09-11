/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.api.rest.resources;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.repository.Permission;
import sonia.scm.repository.Repository;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;


import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Sebastian Sdorra
 */
@Path("repositories")
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public class RepositoryResource extends AbstractResource<Repository>
{

  /** Field description */
  public static final String PATH_PART = "repositories";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public RepositoryResource()
  {
    repositoryStore = new LinkedHashMap<String, Repository>();
    repositoryStore.put("sonia.lib",
                        new Repository("hg", "sonia.lib", "csit@ostfalia.de",
                                       "SONIA Library",
                                       new Permission("csit", true, true,
                                         true)));
    repositoryStore.put("sonia.misc",
                        new Repository("hg", "sonia.misc", "csit@ostfalia.de",
                                       "SONIA Miscelanious",
                                       new Permission("csit", true, true,
                                         true)));
    repositoryStore.put("PWA",
                        new Repository("svn", "PWA",
                                       "csit@fh-wolfenbuettel.de", "PWA",
                                       new Permission("th", true, true),
                                       new Permission("sdorra", true, true),
                                       new Permission("oelkersd", true,
                                         false)));
    repositoryStore.put("sonia.app",
                        new Repository("hg", "sonia.app", "csit@ostfalia.de",
                                       "SONIA Applications",
                                       new Permission("csit", true, true,
                                         true)));
    repositoryStore.put("sonia.webapps",
                        new Repository("hg", "sonia.webapps",
                                       "csit@ostfalia.de",
                                       "SONIA WebApplications",
                                       new Permission("csit", true, true,
                                         true)));
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param item
   */
  @Override
  protected void addItem(Repository item)
  {
    repositoryStore.put(item.getName(), item);
  }

  /**
   * Method description
   *
   *
   * @param item
   */
  @Override
  protected void removeItem(Repository item)
  {
    repositoryStore.remove(item.getName());
  }

  /**
   * Method description
   *
   *
   * @param name
   * @param item
   */
  @Override
  protected void updateItem(String name, Repository item)
  {
    Repository repository = repositoryStore.get(name);

    repository.setContact(item.getContact());
    repository.setDescription(item.getDescription());
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected Repository[] getAllItems()
  {
    Collection<Repository> repositoryCollection = repositoryStore.values();

    return repositoryCollection.toArray(
        new Repository[repositoryCollection.size()]);
  }

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
    return item.getName();
  }

  /**
   * Method description
   *
   *
   * @param name
   *
   * @return
   */
  @Override
  protected Repository getItem(String name)
  {
    return repositoryStore.get(name);
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
  private HashMap<String, Repository> repositoryStore;
}
