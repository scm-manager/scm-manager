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
import sonia.scm.cli.CliContext;
import sonia.scm.cli.ExitCode;
import sonia.scm.cli.Table;
import sonia.scm.cli.TemplateRenderer;
import sonia.scm.group.Group;
import sonia.scm.template.TemplateEngineFactory;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static java.util.Map.entry;

class GroupTemplateRenderer extends TemplateRenderer {

  private static final String NOT_FOUND_TEMPLATE = "{{i18n.groupNotFound}}";

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

  private final GroupCommandBeanMapper mapper;

  @Inject
  GroupTemplateRenderer(CliContext context, TemplateEngineFactory templateEngineFactory, GroupCommandBeanMapper mapper) {
    super(context, templateEngineFactory);
    this.mapper = mapper;
  }

  void render(Group group) {
    GroupCommandBean groupBean = mapper.map(group);
    Table table = createTable();
    String yes = getBundle().getString("yes");
    String no = getBundle().getString("no");
    table.addLabelValueRow("scm.group.name", groupBean.getName());
    table.addLabelValueRow("scm.group.description", groupBean.getDescription());
    table.addLabelValueRow("scm.group.members", groupBean.getMembersList());
    table.addLabelValueRow("scm.group.external", groupBean.isExternal() ? yes : no);
    table.addLabelValueRow("creationDate", groupBean.getCreationDate());
    table.addLabelValueRow("lastModified", groupBean.getLastModified());

    renderToStdout(DETAILS_TABLE_TEMPLATE, ImmutableMap.of("rows", table, "repo", groupBean));
  }

  void renderNotFoundError() {
    renderToStderr(NOT_FOUND_TEMPLATE, Collections.emptyMap());
    getContext().exit(ExitCode.NOT_FOUND);
  }

  public void render(Collection<String> permissions) {
    renderToStdout(PERMISSION_LIST_TEMPLATE, Map.ofEntries(entry("permissions", permissions)));
  }
}
