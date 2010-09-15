/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.api.rest.resources;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Singleton;

import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.RepositoryManager;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Collection;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Sebastian Sdorra
 */
@Path("repositories")
@Singleton
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public class RepositoryResource extends AbstractResource<Repository>
{

  /** Field description */
  public static final String PATH_PART = "repositories";

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param item
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  protected void addItem(Repository item)
          throws RepositoryException, IOException
  {
    repositoryManager.create(item);
  }

  /**
   * Method description
   *
   *
   * @param item
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  protected void removeItem(Repository item)
          throws RepositoryException, IOException
  {
    repositoryManager.delete(item);
  }

  /**
   * Method description
   *
   *
   * @param name
   * @param item
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  protected void updateItem(String name, Repository item)
          throws RepositoryException, IOException
  {
    repositoryManager.modify(item);
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
    Collection<Repository> repositoryCollection = repositoryManager.getAll();

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
    return item.getId();
  }

  /**
   * Method description
   *
   *
   *
   * @param id
   *
   * @return
   */
  @Override
  protected Repository getItem(String id)
  {
    return repositoryManager.get(id);
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
  @Inject
  private RepositoryManager repositoryManager;
}
