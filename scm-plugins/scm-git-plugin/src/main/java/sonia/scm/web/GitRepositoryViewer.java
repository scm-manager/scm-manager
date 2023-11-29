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
    
package sonia.scm.web;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.io.Closeables;
import com.google.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Branch;
import sonia.scm.repository.Branches;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.Person;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngine;
import sonia.scm.template.TemplateEngineFactory;
import sonia.scm.util.HttpUtil;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.Iterator;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public class GitRepositoryViewer
{

  /** Field description */
  public static final String MIMETYPE_HTML = "text/html";

  /** Field description */
  public static final String RESOURCE_GITINDEX =
    "/sonia/scm/git.index.mustache";

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
   */
  @Inject
  public GitRepositoryViewer(TemplateEngineFactory templateEngineFactory,
    RepositoryServiceFactory repositoryServiceFactory)
  {
    this.templateEngineFactory = templateEngineFactory;
    this.repositoryServiceFactory = repositoryServiceFactory;
  }

  //~--- methods --------------------------------------------------------------

  public void handleRequest(HttpServletRequest request,
    HttpServletResponse response, Repository repository)
    throws IOException
  {

    String baseUrl = HttpUtil.getCompleteUrl(request);

    logger.trace("render git repository quick view with base url {}", baseUrl);

    response.setContentType(MIMETYPE_HTML);

    TemplateEngine engine = templateEngineFactory.getDefaultEngine();
    Template template = engine.getTemplate(RESOURCE_GITINDEX);
    //J-
    ImmutableMap<String,Object> env = ImmutableMap.of(
      "repository", repository, 
      "branches", createBranchesModel(repository)
    );
    //J+

    Writer writer = null;

    try
    {
      response.setCharacterEncoding("UTF-8");
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
   */
  private BranchesModel createBranchesModel(Repository repository)
    throws IOException
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
      IOUtil.close(service);
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
  private final RepositoryServiceFactory repositoryServiceFactory;

  /** Field description */
  private final TemplateEngineFactory templateEngineFactory;
}
