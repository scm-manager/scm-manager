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

package sonia.scm.cli;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import picocli.CommandLine;
import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngine;
import sonia.scm.template.TemplateEngineFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.UnaryOperator;

/**
 * This is the default template renderer which should be used to write templated content to the channels of the CLI connection.
 * @since 2.33.0
 */
public class TemplateRenderer {

  @CommandLine.Option(names = {"--template", "-t"}, paramLabel = "TEMPLATE", descriptionKey = "scm.templateRenderer.template")
  private String template;

  private static final String DEFAULT_ERROR_TEMPLATE = String.join("\n",
    "{{i18n.errorCommandFailed}}", "{{i18n.errorUnknownError}}:",
    "{{error}}"
  );

  private final CliContext context;
  private final TemplateEngine templateEngine;
  @Nullable
  @CommandLine.Spec(CommandLine.Spec.Target.MIXEE)
  private CommandLine.Model.CommandSpec spec;

  @Inject
  public TemplateRenderer(CliContext context, TemplateEngineFactory templateEngineFactory) {
    this.context = context;
    this.templateEngine = templateEngineFactory.getDefaultEngine();
  }

  /**
   * Writes templated content to the stdout channel
   * @param template the mustache template
   * @param model the model which should be used for templating
   */
  public void renderToStdout(String template, Map<String, Object> model) {
    exec(context.getStdout(), template, model);
  }

  /**
   * Writes templated content to the stderr channel
   * @param template the mustache template
   * @param model the model which should be used for templating
   */
  public void renderToStderr(String template, Map<String, Object> model) {
    exec(context.getStderr(), template, model);
  }

  /**
   * Writes an error to the stderr channel using the default error template
   * @param error the error which should be used for templating
   */
  public void renderDefaultError(String error) {
    exec(context.getStderr(), DEFAULT_ERROR_TEMPLATE, ImmutableMap.of("error", error));
  }

  /**
   * Writes the exception message to the stderr channel using the default error template
   * @param exception the exception which should be used for templating
   */
  public void renderDefaultError(Exception exception) {
   renderDefaultError(exception.getMessage());
  }

  /**
   * Creates the table which should be used to template table-like content.
   * @return table for templating content
   */
  public Table createTable() {
    return new Table(getBundle());
  }

  protected CliContext getContext() {
    return context;
  }

  protected ResourceBundle getBundle() {
    return getSpecOrDie().resourceBundle();
  }

  private CommandLine.Model.CommandSpec getSpecOrDie() {
    if (spec == null) {
      throw new IllegalStateException("Failed to resolve command spec. Perhaps you forgot to add @Mixin?");
    }
    return spec;
  }

  private void exec(PrintWriter stream, String defaultTemplate, Map<String, Object> model) {
    try {
      Template tpl = templateEngine.getTemplate(getClass().getName(), new StringReader(MoreObjects.firstNonNull(template, defaultTemplate)));
      tpl.execute(stream, createModel(model));
      stream.flush();
    } catch (IOException e) {
      throw new TemplateRenderingException("failed to render template", e);
    }
  }

  private Object createModel(Map<String, Object> model) {
    Map<String, Object> finalModel = new HashMap<>(model);
    finalModel.put("lf", "\n");
    UnaryOperator<String> upper = value -> value.toUpperCase(context.getLocale());
    finalModel.put("upper", upper);

    ResourceBundle resourceBundle = getBundle();
    if (resourceBundle != null) {
      finalModel.put("i18n", new I18n(resourceBundle));
    }
    return Collections.unmodifiableMap(finalModel);
  }

  @VisibleForTesting
  void setSpec(@Nonnull CommandLine.Model.CommandSpec spec) {
    this.spec = spec;
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
