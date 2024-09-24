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

package sonia.scm;

import com.google.common.io.Resources;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URL;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StaticResourceServletTest {

  @Mock
  private HttpServletRequest request;

  @Mock
  private ServletOutputStream stream;

  @Mock
  private HttpServletResponse response;

  @Mock
  private ServletContext context;

  @Test
  void shouldServeResource() throws IOException {
    doReturn("/scm").when(request).getContextPath();
    doReturn("/scm/resource.txt").when(request).getRequestURI();
    doReturn(context).when(request).getServletContext();
    URL resource = Resources.getResource("sonia/scm/lifecycle/resource.txt");
    doReturn(resource).when(context).getResource("/resource.txt");
    doReturn(stream).when(response).getOutputStream();

    StaticResourceServlet servlet = new StaticResourceServlet();
    servlet.doGet(request, response);

    verify(response).setStatus(HttpServletResponse.SC_OK);
  }

  @Test
  void shouldReturnNotFound() {
    doReturn("/scm").when(request).getContextPath();
    doReturn("/scm/resource.txt").when(request).getRequestURI();
    doReturn(context).when(request).getServletContext();

    StaticResourceServlet servlet = new StaticResourceServlet();
    servlet.doGet(request, response);

    verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
  }

}
