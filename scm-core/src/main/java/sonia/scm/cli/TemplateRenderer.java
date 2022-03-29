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

package sonia.scm.cli;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import picocli.CommandLine;
import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngine;
import sonia.scm.template.TemplateEngineFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

public final class TemplateRenderer {

  @CommandLine.Option(names = {"--template", "-t"}, paramLabel = "TEMPLATE", description = "Specify rendering template")
  private String template;

  private static final String DEFAULT_ERROR_TEMPLATE = String.join("\n",
    "{{i18n.errorCommandFailed}}", "{{i18n.errorUnknownError}}:",
    "{{error}}"
  );

  private final CliContext context;
  private final TemplateEngine templateEngine;
  @CommandLine.Spec(CommandLine.Spec.Target.MIXEE)
  private CommandLine.Model.CommandSpec spec;

  @Inject
  public TemplateRenderer(CliContext context, TemplateEngineFactory templateEngineFactory) {
    this.context = context;
    this.templateEngine = templateEngineFactory.getDefaultEngine();
  }

  public void renderToStdout(String defaultTemplate, Map<String, Object> model) {
    exec(context.getStdout(), defaultTemplate, model);

  }

  public void renderToStderr(String defaultTemplate, Map<String, Object> model) {
    exec(context.getStderr(), defaultTemplate, model);
  }

  public void renderDefaultError(String error) {
    exec(context.getStderr(), DEFAULT_ERROR_TEMPLATE, ImmutableMap.of("error", error));
  }

  private void exec(PrintWriter stream, String defaultTemplate, Map<String, Object> model) {
    try {
      Template tpl = templateEngine.getTemplate(getClass().getName(), new StringReader(MoreObjects.firstNonNull(template, defaultTemplate)));
      tpl.execute(stream, createModel(model));
      stream.flush();
    } catch (IOException e) {
      //TODO Handle
      e.printStackTrace();
    }
  }

  private Object createModel(Map<String, Object> model) {
    Map<String, Object> finalModel = new HashMap<>(model);
    finalModel.put("lf", "\n");

    ResourceBundle resourceBundle = spec.resourceBundle();
    if (resourceBundle != null) {
      finalModel.put("i18n", new I18n(resourceBundle));
    }
    return Collections.unmodifiableMap(finalModel);
  }

  @SuppressWarnings("java:S2160") // Do not need equals or hashcode
  private static class I18n extends AbstractMap<String, String> {

    private final ResourceBundle resourceBundle;

    I18n(ResourceBundle resourceBundle) {
      this.resourceBundle = resourceBundle;
    }

    @Override
    public String get(Object key) {
      return resourceBundle.getString(key.toString());
    }

    @Override
    public boolean containsKey(Object key) {
      return resourceBundle.containsKey(key.toString());
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
      throw  new UnsupportedOperationException("Should not be used");
    }
  }
}
