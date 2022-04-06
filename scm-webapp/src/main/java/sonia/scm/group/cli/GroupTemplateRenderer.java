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
import java.util.Collections;

class GroupTemplateRenderer extends TemplateRenderer {

  private static final String NOT_FOUND_TEMPLATE = "{{i18n.groupNotFound}}";

  private static final String DETAILS_TABLE_TEMPLATE = String.join("\n",
    "{{#rows}}",
    "{{#cols}}{{value}}{{/cols}}",
    "{{/rows}}"
  );

  private final GroupCommandBeanMapper mapper;

  @Inject
  public GroupTemplateRenderer(CliContext context, TemplateEngineFactory templateEngineFactory, GroupCommandBeanMapper mapper) {
    super(context, templateEngineFactory);
    this.mapper = mapper;
  }

  public void render(Group group) {
    GroupCommandBean groupBean = mapper.map(group);
    Table table = createTable();
    String yes = getBundle().getString("yes");
    String no = getBundle().getString("no");
    table.addLabelValueRow("groupName", groupBean.getName());
    table.addLabelValueRow("groupDescription", groupBean.getDescription());
    table.addLabelValueRow("groupMembers", groupBean.getMembersList());
    table.addLabelValueRow("groupExternal", groupBean.isExternal() ? yes : no);
    table.addLabelValueRow("groupCreationDate", groupBean.getCreationDate());
    table.addLabelValueRow("groupLastModified", groupBean.getLastModified());

    renderToStdout(DETAILS_TABLE_TEMPLATE, ImmutableMap.of("rows", table, "repo", groupBean));
  }

  public void renderNotFoundError() {
    renderToStderr(NOT_FOUND_TEMPLATE, Collections.emptyMap());
    getContext().exit(ExitCode.NOT_FOUND);
  }
}
