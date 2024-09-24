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


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Singleton;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.filter.WebElement;
import sonia.scm.i18n.I18nCollector;

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
