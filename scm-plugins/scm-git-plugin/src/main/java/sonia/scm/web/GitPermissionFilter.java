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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jgit.http.server.GitSmartHttpTools;
import sonia.scm.ClientMessages;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.spi.ScmProviderHttpServlet;
import sonia.scm.web.filter.PermissionFilter;

import java.io.IOException;

/**
 * GitPermissionFilter decides if a git request requires write or read privileges.
 * 
 */
public class GitPermissionFilter extends PermissionFilter
{

  private static final String PARAMETER_SERVICE = "service";

  private static final String PARAMETER_VALUE_RECEIVE = "git-receive-pack";

  private static final String URI_RECEIVE_PACK = "git-receive-pack";

  private static final String URI_REF_INFO = "/info/refs";
  
  private static final String METHOD_LFS_UPLOAD = "PUT";


  /**
   * Constructs a new instance of the GitPermissionFilter.
   *
   * @param configuration scm main configuration
   */
  public GitPermissionFilter(ScmConfiguration configuration, ScmProviderHttpServlet delegate) {
    super(configuration, delegate);
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
  public boolean isWriteRequest(HttpServletRequest request) {
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
