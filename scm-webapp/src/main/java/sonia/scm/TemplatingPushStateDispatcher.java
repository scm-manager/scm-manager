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
