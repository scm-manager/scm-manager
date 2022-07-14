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


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.filter.WebElement;
import sonia.scm.i18n.I18nCollector;

import javax.inject.Inject;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;


/**
 * Collect the plugin translations.
 */
@Singleton
@WebElement(value = I18nServlet.PATTERN, regex = true)
public class I18nServlet extends HttpServlet {

  private static final Logger LOG = LoggerFactory.getLogger(I18nServlet.class);

  public static final String PLUGINS_JSON = "plugins.json";
  public static final String PATTERN = "/locales/[a-z\\-A-Z]*/" + PLUGINS_JSON;
  public static final int POSITION_OF_LANGUAGE_IN_PATH = 2;

  private final I18nCollector i18nCollector;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Inject
  public I18nServlet(I18nCollector i18nCollector) {
    this.i18nCollector = i18nCollector;
  }

  @VisibleForTesting
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    String path = request.getServletPath();
    try {
      String languageCode = extractLanguage(path);
      Optional<JsonNode> json = i18nCollector.findJson(languageCode);
      if (json.isPresent()) {
        write(response, json.get());
      } else {
        LOG.debug("could not find translation for language {}", languageCode);
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      }
    } catch (IOException ex) {
      LOG.error("Error on getting the translation of the plugins", ex);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  private String extractLanguage(String path) {
    return path.split("/")[POSITION_OF_LANGUAGE_IN_PATH];
  }

  private void write(HttpServletResponse response, JsonNode jsonNode) throws IOException {
    response.setCharacterEncoding("UTF-8");
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");

    try (PrintWriter writer = response.getWriter()) {
      objectMapper.writeValue(writer, jsonNode);
    }
  }
}
