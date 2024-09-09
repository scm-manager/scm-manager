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

import com.google.common.collect.ImmutableSet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sonia.scm.ClientMessages;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.ScmSvnErrorCode;
import sonia.scm.repository.SvnUtil;
import sonia.scm.repository.spi.ScmProviderHttpServlet;
import sonia.scm.web.filter.PermissionFilter;

import java.io.IOException;
import java.util.Set;


public class SvnPermissionFilter extends PermissionFilter
{

  private static final Set<String> WRITEMETHOD_SET =
    ImmutableSet.of("MKACTIVITY", "PROPPATCH", "PUT", "CHECKOUT", "MKCOL",
      "MOVE", "COPY", "DELETE", "LOCK", "UNLOCK", "MERGE");



  public SvnPermissionFilter(ScmConfiguration configuration, ScmProviderHttpServlet delegate)
  {
    super(configuration, delegate);
  }



  @Override
  protected void sendNotEnoughPrivilegesError(HttpServletRequest request,
    HttpServletResponse response)
    throws IOException
  {
    if (SvnUtil.isSvnClient(request))
    {
      //J-
      SvnUtil.sendError(
        request, 
        response, 
        HttpServletResponse.SC_FORBIDDEN,
        ScmSvnErrorCode.authzNotEnoughPrivileges(
          ClientMessages.get(request).notEnoughPrivileges()
        )
      );
      //J+
    }
    else
    {
      super.sendNotEnoughPrivilegesError(request, response);
    }
  }



  @Override
  public boolean isWriteRequest(HttpServletRequest request)
  {
    return WRITEMETHOD_SET.contains(request.getMethod().toUpperCase());
  }
}
