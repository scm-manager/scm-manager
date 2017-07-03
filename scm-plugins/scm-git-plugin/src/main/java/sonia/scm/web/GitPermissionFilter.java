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
import org.eclipse.jgit.http.server.GitSmartHttpTools;
import sonia.scm.ClientMessages;
import sonia.scm.Priority;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.filter.Filters;
import sonia.scm.filter.WebElement;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.RepositoryProvider;
import sonia.scm.web.filter.ProviderPermissionFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//~--- JDK imports ------------------------------------------------------------

/**
 * GitPermissionFilter decides if a git request requires write or read privileges.
 * 
 * @author Sebastian Sdorra
 */
@Priority(Filters.PRIORITY_AUTHORIZATION)
@WebElement(value = GitServletModule.PATTERN_GIT)
public class GitPermissionFilter extends ProviderPermissionFilter
{

  private static final String PARAMETER_SERVICE = "service";

  private static final String PARAMETER_VALUE_RECEIVE = "git-receive-pack";

  private static final String URI_RECEIVE_PACK = "git-receive-pack";

  private static final String URI_REF_INFO = "/info/refs";
  
  private static final String METHOD_LFS_UPLOAD = "PUT";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new instance of the GitPermissionFilter.
   *
   * @param configuration scm main configuration
   * @param repositoryProvider repository provider
   */
  @Inject
  public GitPermissionFilter(ScmConfiguration configuration, RepositoryProvider repositoryProvider) {
    super(configuration, repositoryProvider);
  }

  @Override
  protected void sendNotEnoughPrivilegesError(HttpServletRequest request, HttpServletResponse response) 
    throws IOException {
    if (GitUtil.isGitClient(request)) {
      GitSmartHttpTools.sendError(request, response,
        HttpServletResponse.SC_FORBIDDEN,
        ClientMessages.get(request).notEnoughPrivileges());
    } else {
      super.sendNotEnoughPrivilegesError(request, response);
    }
  }

  @Override
  protected boolean isWriteRequest(HttpServletRequest request) {
    return isReceivePackRequest(request) ||
        isReceiveServiceRequest(request) ||
        isLfsFileUpload(request);
  }
  
  private boolean isReceivePackRequest(HttpServletRequest request) {
    return request.getRequestURI().endsWith(URI_RECEIVE_PACK);
  }
  
  private boolean isReceiveServiceRequest(HttpServletRequest request) {
    return request.getRequestURI().endsWith(URI_REF_INFO) 
        && PARAMETER_VALUE_RECEIVE.equals(request.getParameter(PARAMETER_SERVICE));
  }

  @VisibleForTesting
  private static boolean isLfsFileUpload(HttpServletRequest request) {
    return METHOD_LFS_UPLOAD.equalsIgnoreCase(request.getMethod());
  }

}
