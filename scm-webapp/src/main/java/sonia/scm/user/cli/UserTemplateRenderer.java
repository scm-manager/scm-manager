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

package sonia.scm.user.cli;

import com.google.common.collect.ImmutableMap;
import jakarta.inject.Inject;
import sonia.scm.cli.CliContext;
import sonia.scm.cli.ExitCode;
import sonia.scm.cli.Table;
import sonia.scm.cli.TemplateRenderer;
import sonia.scm.template.TemplateEngineFactory;
import sonia.scm.user.User;

import java.util.Collection;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Map.entry;

class UserTemplateRenderer extends TemplateRenderer {

  private static final String DETAILS_TABLE_TEMPLATE = String.join("\n",
    "{{#rows}}",
    "{{#cols}}{{value}}{{/cols}}",
    "{{/rows}}"
  );

  private static final String PERMISSION_LIST_TEMPLATE = String.join("\n",
    "{{#permissions}}",
    "{{.}}",
    "{{/permissions}}"
  );

  private static final String PASSWORD_ERROR_TEMPLATE = "{{i18n.scmUserErrorPassword}}\n";
  private static final String EXTERNAL_ACTIVATE_TEMPLATE = "{{i18n.scmUserErrorExternalActivate}}\n";
  private static final String EXTERNAL_DEACTIVATE_TEMPLATE = "{{i18n.scmUserErrorExternalDeactivate}}\n";
  private static final String NOT_FOUND_TEMPLATE = "{{i18n.scmUserErrorNotFound}}\n";
  private static final String UNKNOWN_PERMISSION_TEMPLATE = "{{i18n.permissionUnknown}}: {{permission}}\n";


  private final CliContext context;
  private final UserCommandBeanMapper mapper;

  @Inject
  UserTemplateRenderer(CliContext context, TemplateEngineFactory templateEngineFactory, UserCommandBeanMapper mapper) {
    super(context, templateEngineFactory);
    this.context = context;
    this.mapper = mapper;
  }

  public void render(User user) {
    Table table = createTable();

    String yes = getBundle().getString("yes");
    String no = getBundle().getString("no");
    UserCommandBean bean = mapper.map(user);
    table.addLabelValueRow("scm.user.username", bean.getName());
    table.addLabelValueRow("scm.user.displayName", bean.getDisplayName());
    table.addLabelValueRow("scm.user.email", bean.getMail());
    table.addLabelValueRow("scm.user.external", bean.isExternal() ? yes : no);
    table.addLabelValueRow("scm.user.active", bean.isActive() ? yes : no);
    table.addLabelValueRow("creationDate", bean.getCreationDate());
    table.addLabelValueRow("lastModified", bean.getLastModified());
    renderToStdout(DETAILS_TABLE_TEMPLATE, ImmutableMap.of("rows", table, "user", bean));
  }

  public void renderPasswordError() {
    renderToStderr(PASSWORD_ERROR_TEMPLATE, emptyMap());
    context.getStderr().println();
    context.exit(ExitCode.USAGE);
  }

  public void renderExternalActivateError() {
    renderToStderr(EXTERNAL_ACTIVATE_TEMPLATE, emptyMap());
    context.getStderr().println();
    context.exit(ExitCode.USAGE);
  }

  public void renderExternalDeactivateError() {
    renderToStderr(EXTERNAL_DEACTIVATE_TEMPLATE, emptyMap());
    context.getStderr().println();
    context.exit(ExitCode.USAGE);
  }

  public void renderNotFoundError() {
    renderToStderr(NOT_FOUND_TEMPLATE, emptyMap());
    context.getStderr().println();
    context.exit(ExitCode.NOT_FOUND);
  }

  void renderUnknownPermissionError(String permission) {
    renderToStderr(UNKNOWN_PERMISSION_TEMPLATE, Map.of("permission", permission));
    getContext().exit(ExitCode.USAGE);
  }

  public void render(Collection<String> permissions) {
    renderToStdout(PERMISSION_LIST_TEMPLATE, Map.ofEntries(entry("permissions", permissions)));
  }
}
