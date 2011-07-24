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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.jgit.http.server.GitServlet;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.resolver.RepositoryResolver;

import sonia.scm.util.HttpUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class ScmGitServlet extends GitServlet
{

  /** Field description */
  public static final String REGEX_GITHTTPBACKEND =
    "(?x)^/git/(.*/(HEAD|info/refs|objects/(info/[^/]+|[0-9a-f]{2}/[0-9a-f]{38}|pack/pack-[0-9a-f]{40}\\.(pack|idx))|git-(upload|receive)-pack))$";

  /** Field description */
  private static final long serialVersionUID = -7712897339207470674L;

  /** Field description */
  private static final Pattern REGEX_REPOSITORYNAME =
    Pattern.compile("/git/([^/]+)/?.*");

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   * @param repositoryResolver
   * @param receivePackFactory
   */
  @Inject
  public ScmGitServlet(GitRepositoryResolver repositoryResolver,
                       GitReceivePackFactory receivePackFactory)
  {
    this.resolver = repositoryResolver;
    setRepositoryResolver(repositoryResolver);
    setReceivePackFactory(receivePackFactory);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   * @param response
   *
   * @throws IOException
   * @throws ServletException
   */
  @Override
  protected void service(HttpServletRequest request,
                         HttpServletResponse response)
          throws ServletException, IOException
  {
    String uri = HttpUtil.getStrippedURI(request);

    if (uri.matches(REGEX_GITHTTPBACKEND))
    {
      super.service(request, response);
    }
    else
    {
      printGitInformation(request, response);
    }
  }

  /**
   * Method description
   *
   *
   *
   * @param request
   * @param response
   *
   * @throws IOException
   * @throws ServletException
   */
  private void printGitInformation(HttpServletRequest request,
                                   HttpServletResponse response)
          throws ServletException, IOException
  {
    String uri = HttpUtil.getStrippedURI(request);
    Matcher m = REGEX_REPOSITORYNAME.matcher(uri);
    String name = null;
    Repository repository = null;

    try
    {
      if (m.matches())
      {
        name = m.group(1);
        repository = resolver.open(request, name);
      }

      if (repository != null)
      {
        new GitRepositoryViewer().handleRequest(response, repository, name);
      }
      else
      {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
      }
    }
    catch (Exception ex)
    {
      throw new ServletException(ex);
    }
    finally
    {
      if (repository != null)
      {
        repository.close();
      }
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private RepositoryResolver<HttpServletRequest> resolver;
}
