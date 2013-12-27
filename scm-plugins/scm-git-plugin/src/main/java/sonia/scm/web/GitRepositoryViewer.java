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



package sonia.scm.web;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.io.Closeables;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.Branch;
import sonia.scm.repository.Branches;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.Person;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngine;
import sonia.scm.template.TemplateEngineFactory;
import sonia.scm.url.RepositoryUrlProvider;
import sonia.scm.url.UrlProvider;
import sonia.scm.url.UrlProviderFactory;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.Writer;

import java.util.Date;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
public class GitRepositoryViewer
{

  /** Field description */
  public static final String MIMETYPE_HTML = "text/html";

  /** Field description */
  public static final String RESOURCE_GITINDEX = "sonia/scm/git.index.mustache";

  /** Field description */
  private static final int CHANGESET_PER_BRANCH = 10;

  /**
   * the logger for GitRepositoryViewer
   */
  private static final Logger logger =
    LoggerFactory.getLogger(GitRepositoryViewer.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param templateEngineFactory
   * @param repositoryServiceFactory
   * @param configuration
   */
  @Inject
  public GitRepositoryViewer(TemplateEngineFactory templateEngineFactory,
    RepositoryServiceFactory repositoryServiceFactory,
    ScmConfiguration configuration)
  {
    this.templateEngineFactory = templateEngineFactory;
    this.repositoryServiceFactory = repositoryServiceFactory;
    this.configuration = configuration;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   * @param request
   * @param response
   * @param repository
   *
   * @throws IOException
   * @throws RepositoryException
   */
  public void handleRequest(HttpServletRequest request,
    HttpServletResponse response, Repository repository)
    throws RepositoryException, IOException
  {

    String baseUrl = configuration.getBaseUrl();

    UrlProvider urlProvider = UrlProviderFactory.createUrlProvider(baseUrl,
                                UrlProviderFactory.TYPE_WUI);

    response.setContentType(MIMETYPE_HTML);

    RepositoryUrlProvider rup = urlProvider.getRepositoryUrlProvider();

    TemplateEngine engine = templateEngineFactory.getDefaultEngine();
    Template template = engine.getTemplate(RESOURCE_GITINDEX);
    //J-
    ImmutableMap<String,Object> env = ImmutableMap.of(
      "repository", repository, 
      "branches", createBranchesModel(repository),
      "commitViewLink", rup.getChangesetUrl(repository.getId(), 0, 20),
      "sourceViewLink", rup.getBrowseUrl(repository.getId(), null, null)
    );
    //J+

    Writer writer = null;

    try
    {
      writer = response.getWriter();
      template.execute(writer, env);
    }
    finally
    {
      Closeables.close(writer, true);
    }
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  private BranchesModel createBranchesModel(Repository repository)
    throws RepositoryException, IOException
  {
    BranchesModel model = null;
    RepositoryService service = null;

    try
    {
      service = repositoryServiceFactory.create(repository);

      Branches branches = service.getBranchesCommand().getBranches();
      Iterable<BranchModel> branchModels =
        Iterables.transform(branches, new BranchModelTransformer(service));

      model = new BranchesModel(branchModels);
    }
    finally
    {
      Closeables.closeQuietly(service);
    }

    return model;
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 13/02/27
   * @author         Enter your name here...
   */
  private static class BranchModel
  {

    /**
     * Constructs ...
     *
     *
     * @param name
     * @param changesets
     */
    public BranchModel(String name, Iterable<ChangesetModel> changesets)
    {
      this.name = name;
      this.changesets = changesets;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Method description
     *
     *
     * @return
     */
    public Iterable<ChangesetModel> getChangesets()
    {
      return changesets;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getName()
    {
      return name;
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private final Iterable<ChangesetModel> changesets;

    /** Field description */
    private final String name;
  }


  /**
   * Class description
   *
   *
   * @version        Enter version here..., 13/02/27
   * @author         Enter your name here...
   */
  private static class BranchModelTransformer
    implements Function<Branch, BranchModel>
  {

    /**
     * Constructs ...
     *
     *
     * @param service
     */
    public BranchModelTransformer(RepositoryService service)
    {
      this.service = service;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param branch
     *
     * @return
     */
    @Override
    public BranchModel apply(Branch branch)
    {
      BranchModel model = null;
      String name = branch.getName();

      try
      {
        //J-
        ChangesetPagingResult cpr = service.getLogCommand()
                                           .setDisableEscaping(true)
                                           .setBranch(name)
                                           .setPagingLimit(CHANGESET_PER_BRANCH)
                                           .getChangesets();
        
        Iterable<ChangesetModel> changesets = 
          Iterables.transform(cpr, new Function<Changeset,ChangesetModel>()
        {

          @Override
          public ChangesetModel apply(Changeset changeset)
          {
            return new ChangesetModel(changeset);
          }
        });
        //J+

        model = new BranchModel(name, changesets);
      }
      catch (Exception ex)
      {
        logger.error("could not create model for branch: ".concat(name), ex);
      }

      return model;
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private final RepositoryService service;
  }


  /**
   * Class description
   *
   *
   * @version        Enter version here..., 13/02/27
   * @author         Enter your name here...
   */
  private static class BranchesModel implements Iterable<BranchModel>
  {

    /**
     * Constructs ...
     *
     *
     * @param branches
     */
    public BranchesModel(Iterable<BranchModel> branches)
    {
      this.branches = branches;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public Iterator<BranchModel> iterator()
    {
      return branches.iterator();
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private final Iterable<BranchModel> branches;
  }


  /**
   * Class description
   *
   *
   * @version        Enter version here..., 13/02/27
   * @author         Enter your name here...
   */
  private static class ChangesetModel
  {

    /**
     * Constructs ...
     *
     *
     * @param changeset
     */
    public ChangesetModel(Changeset changeset)
    {
      this.changeset = changeset;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Method description
     *
     *
     * @return
     */
    public Person getAuthor()
    {
      return changeset.getAuthor();
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getDate()
    {
      String date = Util.EMPTY_STRING;
      Long time = changeset.getDate();

      if (time != null)
      {
        date = Util.formatDate(new Date(time));
      }

      return date;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getDescription()
    {
      return changeset.getDescription();
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private final Changeset changeset;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final ScmConfiguration configuration;

  /** Field description */
  private final RepositoryServiceFactory repositoryServiceFactory;

  /** Field description */
  private final TemplateEngineFactory templateEngineFactory;
}
