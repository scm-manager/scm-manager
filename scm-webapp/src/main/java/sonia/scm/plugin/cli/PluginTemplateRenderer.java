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

package sonia.scm.plugin.cli;

import sonia.scm.cli.CliContext;
import sonia.scm.cli.ExitCode;
import sonia.scm.cli.TemplateRenderer;
import sonia.scm.template.TemplateEngineFactory;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Map;

class PluginTemplateRenderer extends TemplateRenderer {

  private static final String PLUGIN_NOT_AVAILABLE_ERROR_TEMPLATE = "{{i18n.scmPluginNotAvailable}}";
  private static final String PLUGIN_NOT_INSTALLED_ERROR_TEMPLATE = "{{i18n.scmPluginNotInstalled}}";
  private static final String PLUGIN_ALREADY_INSTALLED_ERROR_TEMPLATE = "{{i18n.scmPluginAlreadyInstalled}}";
  private static final String PLUGIN_NOT_REMOVED_ERROR_TEMPLATE = "{{i18n.scmPluginNotRemoved}}";
  private static final String PLUGIN_NOT_ADDED_ERROR_TEMPLATE = "{{i18n.scmPluginNotAdded}}";
  private static final String PLUGIN_NOT_UPDATABLE_ERROR_TEMPLATE = "{{i18n.scmPluginNotUpdatable}}";
  private static final String PLUGIN_UPDATE_ERROR_TEMPLATE = "{{i18n.scmPluginsUpdateFailed}}";
  private static final String PLUGIN_ADDED_TEMPLATE = "{{i18n.scmPluginAdded}}";
  private static final String PLUGIN_REMOVED_TEMPLATE = "{{i18n.scmPluginRemoved}}";
  private static final String PLUGIN_UPDATED_TEMPLATE = "{{i18n.scmPluginUpdated}}";
  private static final String ALL_PLUGINS_UPDATED_TEMPLATE = "{{i18n.scmPluginsUpdated}}";
  private static final String SERVER_RESTART_REQUIRED_TEMPLATE = "{{i18n.scmServerRestartRequired}}";
  private static final String SERVER_RESTART_TRIGGERED_TEMPLATE = "{{i18n.scmServerRestartTriggered}}";
  private static final String SERVER_RESTART_SKIPPED_TEMPLATE = "{{i18n.scmServerRestartSkipped}}";
  private static final String SERVER_RESTART_CONFIRMATION_TEMPLATE = "{{i18n.scmServerRestartConfirmation}}";
  private static final String PLUGINS_NOT_PENDING_ERROR_TEMPLATE = "{{i18n.scmPluginsNotPending}}";
  private static final String ALL_PENDING_PLUGINS_CANCELLED = "{{i18n.scmPendingPluginsCancelled}}";

  private static final String PLUGIN = "plugin";
  private final CliContext context;

  @Inject
  PluginTemplateRenderer(CliContext context, TemplateEngineFactory templateEngineFactory) {
    super(context, templateEngineFactory);
    this.context = context;
  }

  public void renderPluginAdded(String pluginName) {
    renderToStdout(PLUGIN_ADDED_TEMPLATE, Map.of(PLUGIN, pluginName));
    context.getStdout().println();
  }

  public void renderPluginRemoved(String pluginName) {
    renderToStdout(PLUGIN_REMOVED_TEMPLATE, Map.of(PLUGIN, pluginName));
    context.getStdout().println();
  }

  public void renderPluginUpdated(String pluginName) {
    renderToStdout(PLUGIN_UPDATED_TEMPLATE, Map.of(PLUGIN, pluginName));
    context.getStdout().println();
  }

  public void renderAllPluginsUpdated() {
    renderToStdout(ALL_PLUGINS_UPDATED_TEMPLATE, Collections.emptyMap());
    context.getStdout().println();
  }

  public void renderPluginCouldNotBeRemoved(String pluginName) {
    renderToStderr(PLUGIN_NOT_REMOVED_ERROR_TEMPLATE, Map.of(PLUGIN, pluginName));
    context.getStderr().println();
    context.exit(ExitCode.USAGE);
  }

  public void renderPluginCouldNotBeAdded(String pluginName) {
    renderToStderr(PLUGIN_NOT_ADDED_ERROR_TEMPLATE, Map.of(PLUGIN, pluginName));
    context.getStderr().println();
    context.exit(ExitCode.SERVER_ERROR);
  }

  public void renderPluginNotUpdatable(String pluginName) {
    renderToStderr(PLUGIN_NOT_UPDATABLE_ERROR_TEMPLATE, Map.of(PLUGIN, pluginName));
    context.getStderr().println();
    context.exit(ExitCode.USAGE);
  }

  public void renderServerRestartRequired() {
    renderToStdout(SERVER_RESTART_REQUIRED_TEMPLATE, Collections.emptyMap());
    context.getStdout().println();
  }

  public void renderServerRestartTriggered() {
    renderToStdout(SERVER_RESTART_TRIGGERED_TEMPLATE, Collections.emptyMap());
    context.getStdout().println();
  }

  public void renderSkipServerRestart() {
    renderToStdout(SERVER_RESTART_SKIPPED_TEMPLATE, Collections.emptyMap());
    context.getStdout().println();
  }

  public void renderPluginsReseted() {
    renderToStdout(ALL_PENDING_PLUGINS_CANCELLED, Collections.emptyMap());
    context.getStdout().println();
  }

  public void renderNoPendingPlugins() {
    renderToStderr(PLUGINS_NOT_PENDING_ERROR_TEMPLATE, Collections.emptyMap());
    context.getStderr().println();
  }

  public void renderConfirmServerRestart() {
    renderToStderr(SERVER_RESTART_CONFIRMATION_TEMPLATE, Collections.emptyMap());
    context.getStderr().println();
    context.exit(ExitCode.USAGE);
  }

  public void renderPluginNotAvailableError() {
    renderToStderr(PLUGIN_NOT_AVAILABLE_ERROR_TEMPLATE, Collections.emptyMap());
    context.getStderr().println();
    context.exit(ExitCode.USAGE);
  }

  public void renderPluginsUpdateError() {
    renderToStderr(PLUGIN_UPDATE_ERROR_TEMPLATE, Collections.emptyMap());
    context.getStderr().println();
    context.exit(ExitCode.SERVER_ERROR);
  }

  public void renderPluginNotInstalledError() {
    renderToStderr(PLUGIN_NOT_INSTALLED_ERROR_TEMPLATE, Collections.emptyMap());
    context.getStderr().println();
    context.exit(ExitCode.USAGE);
  }

  public void renderPluginAlreadyInstalledError() {
    renderToStderr(PLUGIN_ALREADY_INSTALLED_ERROR_TEMPLATE, Collections.emptyMap());
    context.getStderr().println();
    context.exit(ExitCode.USAGE);
  }
}
