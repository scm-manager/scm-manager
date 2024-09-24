/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
