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
import sonia.scm.cli.Table;
import sonia.scm.cli.TemplateRenderer;
import sonia.scm.group.Group;
import sonia.scm.template.TemplateEngineFactory;

import javax.inject.Inject;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class GroupTemplateRenderer extends TemplateRenderer {

  private static final String DETAILS_TABLE_TEMPLATE = String.join("\n",
    "{{#rows}}",
    "{{#cols}}{{value}}{{/cols}}",
    "{{/rows}}"
  );

  @Inject
  public GroupTemplateRenderer(CliContext context, TemplateEngineFactory templateEngineFactory) {
    super(context, templateEngineFactory);
  }

  public void render(Group group) {
    Table table = createTable();
    table.addLabelValueRow("groupName", group.getName());
    table.addLabelValueRow("groupDescription", group.getDescription());
    table.addLabelValueRow("groupMembers", String.join(", ", group.getMembers()));
    table.addLabelValueRow("groupExternal", Boolean.toString(group.isExternal()));
    table.addLabelValueRow("groupCreationDate", renderTimestamp(group.getCreationDate()));
    table.addLabelValueRow("groupLastModified", renderTimestamp(group.getLastModified()));

    renderToStdout(DETAILS_TABLE_TEMPLATE, ImmutableMap.of("rows", table, "repo", group));
  }

  private String renderTimestamp(Long timestamp) {
    return timestamp == null ? null : DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(timestamp));
  }
}
