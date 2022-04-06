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

package sonia.scm.group.cli;

import com.google.common.collect.ImmutableMap;
import picocli.CommandLine;
import sonia.scm.cli.ParentCommand;
import sonia.scm.cli.Table;
import sonia.scm.cli.TemplateRenderer;
import sonia.scm.group.GroupManager;
import sonia.scm.repository.cli.GroupCommand;

import javax.inject.Inject;
import java.util.Collection;

import static java.util.stream.Collectors.toList;

@ParentCommand(GroupCommand.class)
@CommandLine.Command(name = "list", aliases = "ls")
class GroupListCommand implements Runnable {

  @CommandLine.Mixin
  private final TemplateRenderer templateRenderer;
  private final GroupManager manager;
  private final GroupBeanMapper beanMapper;

  @CommandLine.Option(names = {"--short", "-s"})
  private boolean useShortTemplate;

  private static final String TABLE_TEMPLATE = String.join("\n",
    "{{#rows}}",
    "{{#cols}}{{#row.first}}{{#upper}}{{value}}{{/upper}}{{/row.first}}{{^row.first}}{{value}}{{/row.first}}{{^last}} {{/last}}{{/cols}}",
    "{{/rows}}"
  );

  private static final String SHORT_TEMPLATE = String.join("\n",
    "{{#repos}}",
    "{{name}}",
    "{{/repos}}"
  );

  @Inject
  public GroupListCommand(TemplateRenderer templateRenderer, GroupManager manager, GroupBeanMapper beanMapper) {
    this.templateRenderer = templateRenderer;
    this.manager = manager;
    this.beanMapper = beanMapper;
  }

  @Override
  public void run() {
    Collection<GroupBean> groupBeans = manager.getAll().stream().map(beanMapper::map).collect(toList());
    if (useShortTemplate) {
      templateRenderer.renderToStdout(SHORT_TEMPLATE, ImmutableMap.of("repos", groupBeans));
    } else {
      Table table = templateRenderer.createTable();
      table.addHeader("groupName", "groupExternal");
      for (GroupBean bean : groupBeans) {
        table.addRow(bean.getName(), bean.getExternal());
      }
      templateRenderer.renderToStdout(TABLE_TEMPLATE, ImmutableMap.of("rows", table, "groups", groupBeans));
    }
  }
}
