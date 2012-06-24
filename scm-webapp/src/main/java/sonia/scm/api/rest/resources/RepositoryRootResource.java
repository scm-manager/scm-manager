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

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.inject.Inject;

import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryTypePredicate;
import sonia.scm.template.TemplateHandler;
import sonia.scm.url.UrlProvider;
import sonia.scm.url.UrlProviderFactory;
import sonia.scm.util.HttpUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.StringWriter;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Sebastian Sdorra
 */
@Path("help/repository-root/{type}.html")
public class RepositoryRootResource
{

  /** Field description */
  public static final String TEMPLATE = "/repository-root";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   * @param templateHandler
   * @param repositoryManager
   */
  @Inject
  public RepositoryRootResource(TemplateHandler templateHandler,
                                RepositoryManager repositoryManager)
  {
    this.templateHandler = templateHandler;
    this.repositoryManager = repositoryManager;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   * @param request
   * @param type
   *
   * @return
   *
   * @throws IOException
   */
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String renderRepositoriesRoot(@Context HttpServletRequest request,
          @PathParam("type") final String type)
          throws IOException
  {
    String baseUrl = HttpUtil.getCompleteUrl(request);
    UrlProvider uiUrlProvider = UrlProviderFactory.createUrlProvider(baseUrl,
                                  UrlProviderFactory.TYPE_WUI);
    //J-
    Collection<RepositoryTemplateElement> unsortedRepositories =
      Collections2.transform( 
        Collections2.filter(
            repositoryManager.getAll(), new RepositoryTypePredicate(type))
        , new RepositoryTransformFunction(uiUrlProvider, baseUrl)
      );
    
    List<RepositoryTemplateElement> repositories = Ordering.from(
      new RepositoryTemplateElementComparator()
    ).sortedCopy(unsortedRepositories);
    //J+
    Map<String, Object> environment = Maps.newHashMap();

    environment.put("repositories", repositories);

    StringWriter writer = new StringWriter();

    templateHandler.render(TEMPLATE, writer, environment);

    return writer.toString();
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 12/05/28
   * @author         Enter your name here...
   */
  public static class RepositoryTemplateElement
  {

    /**
     * Constructs ...
     *
     *
     * @param repository
     * @param uiUrlProvider
     * @param baseUrl
     */
    public RepositoryTemplateElement(Repository repository,
                                     UrlProvider uiUrlProvider, String baseUrl)
    {
      this.repository = repository;
      this.urlProvider = uiUrlProvider;
      this.baseUrl = baseUrl;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Method description
     *
     *
     * @return
     */
    public String getCommitUrl()
    {
      return urlProvider.getRepositoryUrlProvider().getChangesetUrl(
          repository.getId(), 0, 20);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getDetailUrl()
    {
      return urlProvider.getRepositoryUrlProvider().getDetailUrl(
          repository.getId());
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getName()
    {
      return repository.getName();
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public Repository getRepository()
    {
      return repository;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getSourceUrl()
    {
      return urlProvider.getRepositoryUrlProvider().getBrowseUrl(
          repository.getId(), null, null);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getUrl()
    {
      return repository.createUrl(baseUrl);
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private String baseUrl;

    /** Field description */
    private Repository repository;

    /** Field description */
    private UrlProvider urlProvider;
  }


  /**
   * Class description
   *
   *
   * @version        Enter version here..., 12/05/29
   * @author         Enter your name here...
   */
  private static class RepositoryTemplateElementComparator
          implements Comparator<RepositoryTemplateElement>
  {

    /**
     * Method description
     *
     *
     * @param left
     * @param right
     *
     * @return
     */
    @Override
    public int compare(RepositoryTemplateElement left,
                       RepositoryTemplateElement right)
    {
      return left.getName().compareTo(right.getName());
    }
  }


  /**
   * Class description
   *
   *
   * @version        Enter version here..., 12/05/28
   * @author         Enter your name here...
   */
  private static class RepositoryTransformFunction
          implements Function<Repository, RepositoryTemplateElement>
  {

    /**
     * Constructs ...
     *
     *
     *
     *
     * @param request
     * @param repositoryManager
     * @param urlProvider
     * @param baseUrl
     */
    public RepositoryTransformFunction(UrlProvider urlProvider, String baseUrl)
    {
      this.urlProvider = urlProvider;
      this.baseUrl = baseUrl;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param repository
     *
     * @return
     */
    @Override
    public RepositoryTemplateElement apply(Repository repository)
    {
      return new RepositoryTemplateElement(repository, urlProvider, baseUrl);
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private String baseUrl;

    /** Field description */
    private UrlProvider urlProvider;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private RepositoryManager repositoryManager;

  /** Field description */
  private TemplateHandler templateHandler;
}
