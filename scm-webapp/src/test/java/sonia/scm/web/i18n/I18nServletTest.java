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

package sonia.scm.web.i18n;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.CharMatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.i18n.I18nCollector;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class I18nServletTest {

  private static final String GIT_PLUGIN =
    json(
      "{",
        "'scm-git-plugin': {",
        "'information': {",
        "'clone' : 'Clone',",
        "'create' : 'Create',",
        "'replace' : 'Push'",
        "}",
        "}",
        "}"
    );

  private static String json(String... parts) {
    return String.join("\n", parts).replaceAll("'", "\"");
  }

  @Mock
  private I18nCollector collector;
  @InjectMocks
  private I18nServlet servlet;

  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;


  @Test
  void shouldFailWith404OnMissingResources() throws IOException {
    String path = "/locales/de/plugins.json";
    when(request.getServletPath()).thenReturn(path);
    when(collector.findJson("de")).thenReturn(empty());

    servlet.doGet(request, response);

    verify(response).setStatus(404);
  }

  @Test
  void shouldReturnJson() throws IOException {
    String path = "/locales/de/plugins.json";
    when(request.getServletPath()).thenReturn(path);
    when(collector.findJson("de")).thenReturn(of(new ObjectMapper().readTree(GIT_PLUGIN)));

    String json = doGetString(servlet, request, response);

    verifyHeaders(response);
    assertJson(json);
  }

  private String doGetString(I18nServlet servlet, HttpServletRequest request, HttpServletResponse response) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintWriter writer = new PrintWriter(baos);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);

    writer.flush();
    return baos.toString(StandardCharsets.UTF_8.name());
  }

  private void verifyHeaders(HttpServletResponse response) {
    verify(response).setCharacterEncoding("UTF-8");
    verify(response).setContentType("application/json");
    verify(response).setHeader("Cache-Control", "no-cache");
  }

  private void assertJson(String actual) {
    assertThat(actual)
      .isNotEmpty()
      .contains(CharMatcher.whitespace().removeFrom(GIT_PLUGIN.substring(1, GIT_PLUGIN.length() - 1)));
  }
}
