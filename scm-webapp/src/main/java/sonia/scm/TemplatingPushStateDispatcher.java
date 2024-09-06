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

import com.google.common.annotations.VisibleForTesting;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngine;
import sonia.scm.template.TemplateEngineFactory;

import java.io.IOException;
import java.io.Writer;

/**
 * This dispatcher renders the /index.mustache template, which is merged in from the scm-ui package.
 *
 * @since 2.0.0
 */
public class TemplatingPushStateDispatcher implements PushStateDispatcher {

  @VisibleForTesting
  static final String TEMPLATE = "/index.mustache";

  private final TemplateEngine templateEngine;
  private final SCMContextProvider context;

  @Inject
  public TemplatingPushStateDispatcher(TemplateEngineFactory templateEngineFactory, SCMContextProvider context) {
    this(templateEngineFactory.getDefaultEngine(), context);
  }

  @VisibleForTesting
  TemplatingPushStateDispatcher(TemplateEngine templateEngine, SCMContextProvider context) {
    this.templateEngine = templateEngine;
    this.context = context;
  }

  @Override
  public void dispatch(HttpServletRequest request, HttpServletResponse response, String uri) throws IOException {
    response.setContentType("text/html");
    response.setCharacterEncoding("UTF-8");

    Template template = templateEngine.getTemplate(TEMPLATE);
    try (Writer writer = response.getWriter()) {
      template.execute(writer, new IndexHtmlModel(request, context.getStage()));
    }
  }

  @VisibleForTesting
  static class IndexHtmlModel {

    private final HttpServletRequest request;
    private final Stage scmStage;

    private IndexHtmlModel(HttpServletRequest request, Stage scmStage) {
      this.request = request;
      this.scmStage = scmStage;
    }

    public String getContextPath() {
      return request.getContextPath();
    }

    public String getScmStage() {
      return scmStage.name();
    }

    public String getLiveReloadURL() {
      return System.getProperty("livereload.url");
    }

  }
}
