package sonia.scm;

import com.google.common.annotations.VisibleForTesting;
import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngine;
import sonia.scm.template.TemplateEngineFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

  @Inject
  public TemplatingPushStateDispatcher(TemplateEngineFactory templateEngineFactory) {
    this(templateEngineFactory.getDefaultEngine());
  }

  @VisibleForTesting
  TemplatingPushStateDispatcher(TemplateEngine templateEngine) {
    this.templateEngine = templateEngine;
  }

  @Override
  public void dispatch(HttpServletRequest request, HttpServletResponse response, String uri) throws IOException {
    response.setContentType("text/html");
    response.setCharacterEncoding("UTF-8");

    Template template = templateEngine.getTemplate(TEMPLATE);
    try (Writer writer = response.getWriter()) {
      template.execute(writer, new IndexHtmlModel(request));
    }
  }

  @VisibleForTesting
  static class IndexHtmlModel {

    private final HttpServletRequest request;

    private IndexHtmlModel(HttpServletRequest request) {
      this.request = request;
    }

    public String getContextPath() {
      return request.getContextPath();
    }

    public String getLiveReloadURL() {
      return System.getProperty("livereload.url");
    }

  }
}
