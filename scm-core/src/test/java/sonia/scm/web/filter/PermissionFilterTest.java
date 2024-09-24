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

package sonia.scm.web.filter;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.Rule;
import org.junit.Test;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.Repository;
import sonia.scm.repository.spi.ScmProviderHttpServlet;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SubjectAware(configuration = "classpath:sonia/scm/shiro.ini")
public class PermissionFilterTest {

  public static final Repository REPOSITORY = new Repository("1", "git", "space", "name");

  @Rule
  public final ShiroRule shiroRule = new ShiroRule();

  private final ScmProviderHttpServlet delegateServlet = mock(ScmProviderHttpServlet.class);

  private final PermissionFilter permissionFilter = new PermissionFilter(new ScmConfiguration(), delegateServlet) {
    @Override
    protected boolean isWriteRequest(HttpServletRequest request) {
      return writeRequest;
    }
  };

  private final HttpServletRequest request = mock(HttpServletRequest.class);
  private final HttpServletResponse response = mock(HttpServletResponse.class);

  private boolean writeRequest = false;

  @Test
  @SubjectAware(username = "reader", password = "secret")
  public void shouldPassForReaderOnReadRequest() throws IOException, ServletException {
    writeRequest = false;

    permissionFilter.service(request, response, REPOSITORY);

    verify(delegateServlet).service(request, response, REPOSITORY);
  }

  @Test
  @SubjectAware(username = "reader", password = "secret")
  public void shouldBlockForReaderOnWriteRequest() throws IOException, ServletException {
    writeRequest = true;

    permissionFilter.service(request, response, REPOSITORY);

    verify(response).sendError(eq(403));
    verify(delegateServlet, never()).service(request, response, REPOSITORY);
  }

  @Test
  @SubjectAware(username = "_anonymous", password = "secret")
  public void shouldBlockForAnonymousOnWriteRequestWithAuthenticationRequest() throws IOException, ServletException {
    writeRequest = true;

    permissionFilter.service(request, response, REPOSITORY);

    verify(response).sendError(eq(401), anyString());
    verify(delegateServlet, never()).service(request, response, REPOSITORY);
  }

  @Test
  @SubjectAware(username = "writer", password = "secret")
  public void shouldPassForWriterOnWriteRequest() throws IOException, ServletException {
    writeRequest = true;

    permissionFilter.service(request, response, REPOSITORY);

    verify(delegateServlet).service(request, response, REPOSITORY);
  }
}
