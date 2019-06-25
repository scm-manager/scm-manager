package sonia.scm.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngine;
import sonia.scm.template.TemplateEngineFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
