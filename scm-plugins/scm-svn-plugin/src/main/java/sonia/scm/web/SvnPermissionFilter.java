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

/**
 *
 * @author Sebastian Sdorra
 */
public class SvnPermissionFilter extends PermissionFilter
{

  /** Field description */
  private static final Set<String> WRITEMETHOD_SET =
    ImmutableSet.of("MKACTIVITY", "PROPPATCH", "PUT", "CHECKOUT", "MKCOL",
      "MOVE", "COPY", "DELETE", "LOCK", "UNLOCK", "MERGE");

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   * @param configuration
   */
  public SvnPermissionFilter(ScmConfiguration configuration, ScmProviderHttpServlet delegate)
  {
    super(configuration, delegate);
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
   */
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

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   */
  @Override
  public boolean isWriteRequest(HttpServletRequest request)
  {
    return WRITEMETHOD_SET.contains(request.getMethod().toUpperCase());
  }
}
