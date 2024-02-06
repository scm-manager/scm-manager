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


public class GitRepositoryViewer
{

  public static final String MIMETYPE_HTML = "text/html";

  public static final String RESOURCE_GITINDEX =
    "/sonia/scm/git.index.mustache";

  private static final int CHANGESET_PER_BRANCH = 10;

 
  private static final Logger logger =
    LoggerFactory.getLogger(GitRepositoryViewer.class);

  private final RepositoryServiceFactory repositoryServiceFactory;

  private final TemplateEngineFactory templateEngineFactory;
 
  @Inject
  public GitRepositoryViewer(TemplateEngineFactory templateEngineFactory,
    RepositoryServiceFactory repositoryServiceFactory)
  {
    this.templateEngineFactory = templateEngineFactory;
    this.repositoryServiceFactory = repositoryServiceFactory;
  }


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




  private static class BranchModel
  {

    private final Iterable<ChangesetModel> changesets;

    private final String name;

    public BranchModel(String name, Iterable<ChangesetModel> changesets)
    {
      this.name = name;
      this.changesets = changesets;
    }

    

  
    public Iterable<ChangesetModel> getChangesets()
    {
      return changesets;
    }

  
    public String getName()
    {
      return name;
    }

  }



  private static class BranchModelTransformer
    implements Function<Branch, BranchModel>
  {
    private final RepositoryService service;
  
    public BranchModelTransformer(RepositoryService service)
    {
      this.service = service;
    }

    

    
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

  }



  private static class BranchesModel implements Iterable<BranchModel>
  {
    private final Iterable<BranchModel> branches;
  
    public BranchesModel(Iterable<BranchModel> branches)
    {
      this.branches = branches;
    }

    

  
    @Override
    public Iterator<BranchModel> iterator()
    {
      return branches.iterator();
    }

  }



  private static class ChangesetModel
  {

    private final Changeset changeset;

    public ChangesetModel(Changeset changeset)
    {
      this.changeset = changeset;
    }


    public Person getAuthor()
    {
      return changeset.getAuthor();
    }

  
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

  
    public String getDescription()
    {
      return changeset.getDescription();
    }

  }

}
