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

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.jgit.http.server.GitServlet;

import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;


import org.eclipse.jgit.lfs.lib.Constants;
import static org.eclipse.jgit.lfs.lib.Constants.CONTENT_TYPE_GIT_LFS_JSON;

import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryProvider;
import sonia.scm.repository.RepositoryRequestListenerUtil;
import sonia.scm.util.HttpUtil;
import sonia.scm.web.lfs.servlet.LfsServletFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sonia.scm.repository.RepositoryException;

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

  /** the logger for ScmGitServlet */
  private static final Logger logger =
    getLogger(ScmGitServlet.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   * @param repositoryResolver
   * @param receivePackFactory
   * @param repositoryViewer
   * @param repositoryProvider
   * @param repositoryRequestListenerUtil
   * @param lfsServletFactory
   */
  @Inject
  public ScmGitServlet(GitRepositoryResolver repositoryResolver,
                       GitReceivePackFactory receivePackFactory,
                       GitRepositoryViewer repositoryViewer,
                       RepositoryProvider repositoryProvider,
                       RepositoryRequestListenerUtil repositoryRequestListenerUtil,
                       LfsServletFactory lfsServletFactory)
  {
    this.repositoryProvider = repositoryProvider;
    this.repositoryViewer = repositoryViewer;
    this.repositoryRequestListenerUtil = repositoryRequestListenerUtil;
    this.lfsServletFactory = lfsServletFactory;

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
    Repository repository = repositoryProvider.get();
    if (repository != null) {
      handleRequest(request, response, repository);
    } else {
      // logger
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
  }
  
  /**
   * Decides the type request being currently made and delegates it accordingly.
   * <ul>
   * <li>Batch API:</li>
   * <ul>
   * <li>used to provide the client with information on how handle the large files of a repository.</li>
   * <li>response contains the information where to perform the actual upload and download of the large objects.</li>
   * </ul>
   * <li>Transfer API:</li>
   * <ul>
   * <li>receives and provides the actual large objects (resolves the pointer placed in the file of the working copy).</li>
   * <li>invoked only after the Batch API has been questioned about what to do with the large files</li>
   * </ul>
   * <li>Regular Git Http API:</li>
   * <ul>
   * <li>regular git http wire protocol, use by normal git clients.</li>
   * </ul>
   * <li>Browser Overview:<li>
   * <ul>
   * <li>short repository overview for browser clients.</li>
   * </ul>
   * </li>
   * </ul>
   */
  private void handleRequest(HttpServletRequest request, HttpServletResponse response, Repository repository) throws ServletException, IOException {
    logger.trace("--- Repository is: {}", repository.getName());
    if (isLfsBatchApiRequest(request, repository.getName())) {
      
      logger.trace("--- detected LFS Batch API Request");
      handleGitLfsRequest(request, response, repository);
    } else if (isLfsFileTransferRequest(request, repository.getName())) {

      logger.trace("--- detected LFS File Transfer Request");
      handleGitLfsRequest(request, response, repository);
    } else if (isRegularGitAPIRequest(request)) {
      logger.trace("--- seems to be regular Git HTTP backend request: {}", request.getRequestURI());
      // continue with the regular git Backend
      handleRegularGitRequest(request, response, repository);
    } else {
      renderHtmlRepositryOverview(request, response);
    }
  }
  
  private boolean isRegularGitAPIRequest(HttpServletRequest request) {
    return HttpUtil.getStrippedURI(request).matches(REGEX_GITHTTPBACKEND);
  }
  
  private void handleGitLfsRequest(HttpServletRequest request, HttpServletResponse response, Repository repository) throws ServletException, IOException {
    HttpServlet servlet = lfsServletFactory.createProtocolServletFor(repository, request);
    if (repositoryRequestListenerUtil.callListeners(request, response, repository)) {
      servlet.service(request, response);
    } else if (logger.isDebugEnabled()) {
      logger.debug("request aborted by repository request listener");
    }
  }
  
  private void handleRegularGitRequest(HttpServletRequest request, HttpServletResponse response, Repository repository) throws ServletException, IOException {
    if (repositoryRequestListenerUtil.callListeners(request, response, repository)) {
      super.service(request, response);
    } else if (logger.isDebugEnabled()) {
      logger.debug("request aborted by repository request listener");
    }
  }
  

  /**
   * This method renders basic information about the repository into the response. The result is meant to be viewed by
   * browser.
   * @param request
   * @param response
   *
   * @throws IOException
   * @throws ServletException
   */
  private void renderHtmlRepositryOverview(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    sonia.scm.repository.Repository scmRepository = repositoryProvider.get();

    if (scmRepository != null)
    {
      try
      {
        repositoryViewer.handleRequest(request, response, scmRepository);
      }
      catch (RepositoryException ex)
      {
        throw new ServletException("could not create repository view", ex);
      }
      catch (IOException ex)
      {
        throw new ServletException("could not create repository view", ex);
      }
    }
    else
    {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
  }

  /**
   * Decides whether or not a request is for the LFS Batch API,
   * <p>
   * - PUT or GET
   * - exactly for this repository
   * - Content Type is {@link Constants#HDR_APPLICATION_OCTET_STREAM}.
   *
   * @return Returns {@code false} if either of the conditions does not match. Returns true if all match.
   */
  private static boolean isLfsFileTransferRequest(HttpServletRequest request, String repository) {

    String regex = String.format("^%s%s/%s(\\.git)?/info/lfs/objects/[a-z0-9]{64}$", request.getContextPath(), GitServletModule.GIT_PATH, repository);
    boolean pathMatches = request.getRequestURI().matches(regex);

    boolean methodMatches = request.getMethod().equals("PUT") || request.getMethod().equals("GET");

    return pathMatches && methodMatches;
  }

  /**
   * Decides whether or not a request is for the LFS Batch API,
   * <p>
   * - POST
   * - exactly for this repository
   * - Content Type is {@link Constants#CONTENT_TYPE_GIT_LFS_JSON}.
   *
   * @return Returns {@code false} if either of the conditions does not match. Returns true if all match.
   */
  private static boolean isLfsBatchApiRequest(HttpServletRequest request, String repository) {

    String regex = String.format("^%s%s/%s(\\.git)?/info/lfs/objects/batch$", request.getContextPath(), GitServletModule.GIT_PATH, repository);
    boolean pathMatches = request.getRequestURI().matches(regex);

    boolean methodMatches = "POST".equals(request.getMethod());

    boolean headerContentTypeMatches = isLfsContentHeaderField(request.getContentType(), CONTENT_TYPE_GIT_LFS_JSON);
    boolean headerAcceptMatches = isLfsContentHeaderField(request.getHeader("Accept"), CONTENT_TYPE_GIT_LFS_JSON);

    return pathMatches && methodMatches && headerContentTypeMatches && headerAcceptMatches;
  }

  /**
   * Checks whether request is of the specific content type.
   *
   * @param request             The HTTP request header value to be examined.
   * @param expectedContentType The expected content type.
   * @return Returns {@code true} if the request has the expected content type. Return {@code false} otherwise.
   */
  @VisibleForTesting
  static boolean isLfsContentHeaderField(String request, String expectedContentType) {

    if (request == null || request.isEmpty()) {
      return false;
    }

    String[] parts = request.split(" ");
    for (String part : parts) {
      if (part.startsWith(expectedContentType)) {

        return true;
      }
    }

    return false;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final RepositoryProvider repositoryProvider;

  /** Field description */
  private final RepositoryRequestListenerUtil repositoryRequestListenerUtil;

  /**
   * Field description
   */
  private final GitRepositoryViewer repositoryViewer;

  private final LfsServletFactory lfsServletFactory;

}
