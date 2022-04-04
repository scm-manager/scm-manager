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

package sonia.scm.web.i18n;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.CharMatcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.i18n.I18nCollector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
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
