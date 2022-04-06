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
import picocli.CommandLine;
import sonia.scm.cli.ParentCommand;
import sonia.scm.cli.Table;
import sonia.scm.cli.TemplateRenderer;
import sonia.scm.user.UserManager;

import javax.inject.Inject;
import java.util.Collection;
import java.util.stream.Collectors;

@ParentCommand(value = UserCommand.class)
@CommandLine.Command(name = "list", aliases = "ls")
public class UserListCommand implements Runnable {

  @CommandLine.Mixin
  private final TemplateRenderer templateRenderer;
  private final UserManager manager;
  private final UserToUserCommandBeanMapper mapper;

  @CommandLine.Option(names = {"--short", "-s"})
  private boolean useShortTemplate;

  private static final String TABLE_TEMPLATE = String.join("\n",
    "{{#rows}}",
    "{{#cols}}{{#row.first}}{{#upper}}{{value}}{{/upper}}{{/row.first}}{{^row.first}}{{value}}{{/row.first}}{{^last}} {{/last}}{{/cols}}",
    "{{/rows}}"
  );

  private static final String SHORT_TEMPLATE = String.join("\n",
    "{{#users}}",
    "{{name}}",
    "{{/users}}"
  );

  @Inject
  public UserListCommand(UserManager manager, TemplateRenderer templateRenderer, UserToUserCommandBeanMapper mapper) {
    this.manager = manager;
    this.templateRenderer = templateRenderer;
    this.mapper = mapper;
  }

  @Override
  public void run() {
    Collection<UserCommandBean> beans = manager.getAll().stream().map(mapper::map).collect(Collectors.toList());
    if (useShortTemplate) {
      templateRenderer.renderToStdout(SHORT_TEMPLATE, ImmutableMap.of("users", beans));
    } else {
      Table table = templateRenderer.createTable();
      String yes = table.getLocalizedValue("yes");
      String no = table.getLocalizedValue("no");
      table.addHeader("scm.user.username", "scm.user.displayName", "scm.user.email", "scm.user.external", "scm.user.active");
      for (UserCommandBean bean : beans) {
        table.addRow(bean.getName(), bean.getDisplayName(), bean.getMail(), bean.isExternal() ? yes : no, bean.isActive() ? yes : no);
      }
      templateRenderer.renderToStdout(TABLE_TEMPLATE, ImmutableMap.of("rows", table, "users", beans));
    }
  }

}
