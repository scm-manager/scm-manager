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
