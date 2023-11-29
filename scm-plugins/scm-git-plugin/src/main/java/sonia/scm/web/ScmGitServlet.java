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

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.jgit.http.server.GitServlet;
import org.eclipse.jgit.lfs.lib.Constants;
import org.slf4j.Logger;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryRequestListenerUtil;
import sonia.scm.repository.spi.ScmProviderHttpServlet;
import sonia.scm.util.HttpUtil;
import sonia.scm.web.lfs.servlet.LfsServletFactory;

import java.io.IOException;
import java.util.regex.Pattern;

import static org.eclipse.jgit.lfs.lib.Constants.CONTENT_TYPE_GIT_LFS_JSON;
import static org.slf4j.LoggerFactory.getLogger;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class ScmGitServlet extends GitServlet implements ScmProviderHttpServlet
{

  public static final String REPO_PATH = "/repo";

  public static final Pattern REGEX_GITHTTPBACKEND = Pattern.compile(
    "(?x)^/repo/(.*/(HEAD|info/refs|objects/(info/[^/]+|[0-9a-f]{2}/[0-9a-f]{38}|pack/pack-[0-9a-f]{40}\\.(pack|idx))|git-(upload|receive)-pack))$"
  );

  /** Field description */
  private static final long serialVersionUID = -7712897339207470674L;

  /** the logger for ScmGitServlet */
  private static final Logger logger = getLogger(ScmGitServlet.class);
  public static final MediaType LFS_LOCKING_MEDIA_TYPE = MediaType.valueOf("application/vnd.git-lfs+json");

  //~--- constructors ---------------------------------------------------------

  @Inject
  public ScmGitServlet(GitRepositoryResolver repositoryResolver,
                       GitReceivePackFactory receivePackFactory,
                       GitRepositoryViewer repositoryViewer,
                       RepositoryRequestListenerUtil repositoryRequestListenerUtil,
                       LfsServletFactory lfsServletFactory)
  {
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
  public void service(HttpServletRequest request, HttpServletResponse response, Repository repository)
    throws ServletException, IOException
  {
    String repoPath = repository.getNamespace() + "/" + repository.getName();
    logger.trace("handle git repository at {}", repoPath);
    if (isLfsBatchApiRequest(request, repoPath)) {
      HttpServlet servlet = lfsServletFactory.createProtocolServletFor(repository, request);
      logger.trace("handle lfs batch api request");
      handleGitLfsRequest(servlet, request, response, repository);
    } else if (isLfsFileTransferRequest(request, repoPath)) {
      HttpServlet servlet = lfsServletFactory.createFileLfsServletFor(repository, request);
      logger.trace("handle lfs file transfer request");
      handleGitLfsRequest(servlet, request, response, repository);
    } else if (isLfsLockingAPIRequest(request)) {
      HttpServlet servlet = lfsServletFactory.createLockServletFor(repository);
      logger.trace("handle lfs lock request");
      handleGitLfsLockingRequest(servlet, request, response, repository);
    } else if (isRegularGitAPIRequest(request)) {
      logger.trace("handle regular git request");
      // continue with the regular git Backend
      handleRegularGitRequest(request, response, repository);
    } else {
      logger.trace("handle browser request");
      handleBrowserRequest(request, response, repository);
    }
  }

  private void handleGitLfsLockingRequest(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response, Repository repository) throws ServletException, IOException {
    if (repositoryRequestListenerUtil.callListeners(request, response, repository)) {
      servlet.service(request, response);
    } else if (logger.isDebugEnabled()) {
      logger.debug("request aborted by repository request listener");
    }
  }

  private boolean isRegularGitAPIRequest(HttpServletRequest request) {
    return REGEX_GITHTTPBACKEND.matcher(HttpUtil.getStrippedURI(request)).matches();
  }

  private boolean isLfsLockingAPIRequest(HttpServletRequest request) {
      return isLfsLockingMediaType(request, "Content-Type")
        || isLfsLockingMediaType(request, "Accept");
  }

  private boolean isLfsLockingMediaType(HttpServletRequest request, String header) {
    try {
      MediaType requestMediaType = MediaType.valueOf(request.getHeader(header));
      return !requestMediaType.isWildcardType()
        && !requestMediaType.isWildcardSubtype()
        && LFS_LOCKING_MEDIA_TYPE.isCompatible(requestMediaType);
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  private void handleGitLfsRequest(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response, Repository repository) throws ServletException, IOException {
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
  private void handleBrowserRequest(HttpServletRequest request, HttpServletResponse response, Repository repository) throws ServletException {
    try {
      repositoryViewer.handleRequest(request, response, repository);
    } catch (IOException ex) {
      throw new ServletException("could not create repository view", ex);
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

    String regex = String.format("^%s%s/%s(\\.git)?/info/lfs/objects/[a-z0-9]{64}$", request.getContextPath(), REPO_PATH, repository);
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

    String regex = String.format("^%s%s/%s(\\.git)?/info/lfs/objects/batch$", request.getContextPath(), REPO_PATH, repository);
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
  private final RepositoryRequestListenerUtil repositoryRequestListenerUtil;

  /**
   * Field description
   */
  private final GitRepositoryViewer repositoryViewer;

  private final LfsServletFactory lfsServletFactory;
}
