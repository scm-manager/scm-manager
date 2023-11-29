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
    
package sonia.scm.lifecycle.view;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngine;
import sonia.scm.template.TemplateEngineFactory;

import java.io.IOException;
import java.io.PrintWriter;

@Singleton
public class SingleViewServlet extends HttpServlet {

  private static final Logger LOG = LoggerFactory.getLogger(SingleViewServlet.class);

  private final Template template;
  private final ViewController controller;

  @Inject
  public SingleViewServlet(TemplateEngineFactory templateEngineFactory, ViewController controller) {
    template = createTemplate(templateEngineFactory, controller.getTemplate());
    this.controller = controller;
  }

  private Template createTemplate(TemplateEngineFactory templateEngineFactory, String template) {
    TemplateEngine engine = templateEngineFactory.getEngineByExtension(template);
    try {
      return engine.getTemplate(template);
    } catch (IOException e) {
      throw new IllegalStateException("failed to parse template: " + template, e);
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
    process(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
    process(req, resp);
  }

  private void process(HttpServletRequest request, HttpServletResponse response) {
    View view = controller.createView(request);

    response.setStatus(view.getStatusCode());
    response.setContentType("text/html");
    response.setCharacterEncoding("UTF-8");

    try (PrintWriter writer = response.getWriter()) {
      template.execute(writer, view.getModel());
    } catch (IOException ex) {
      LOG.error("failed to write view", ex);
    }
  }
}
