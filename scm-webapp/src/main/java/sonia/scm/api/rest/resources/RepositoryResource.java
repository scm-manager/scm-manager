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
import com.google.inject.Singleton;

import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.RepositoryHandler;
import sonia.scm.repository.RepositoryManager;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

/**
 *
 * @author Sebastian Sdorra
 */
@Path("repositories")
@Singleton
public class RepositoryResource extends AbstractResource<Repository>
{

  /** Field description */
  public static final String PATH_PART = "repositories";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param configuration
   * @param repositoryManager
   */
  @Inject
  public RepositoryResource(ScmConfiguration configuration,
                            RepositoryManager repositoryManager)
  {
    this.configuration = configuration;
    this.repositoryManager = repositoryManager;
  }

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
    Repository[] repositories =
      repositoryCollection.toArray(new Repository[repositoryCollection.size()]);

    for (Repository repository : repositories)
    {
      appendUrl(repository);
    }

    return repositories;
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
    Repository repository = repositoryManager.get(id);

    appendUrl(repository);

    return repository;
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
      StringBuilder url = new StringBuilder(request.getScheme());

      url.append("://").append(configuration.getServername());
      url.append(":").append(request.getLocalPort());

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

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private ScmConfiguration configuration;

  /** Field description */
  private RepositoryManager repositoryManager;

  /** TODO path request direct to method */
  @Context
  private HttpServletRequest request;
}
