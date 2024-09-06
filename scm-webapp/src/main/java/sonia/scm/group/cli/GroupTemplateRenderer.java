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

package sonia.scm.group.cli;

import com.google.common.collect.ImmutableMap;
import jakarta.inject.Inject;
import sonia.scm.cli.CliContext;
import sonia.scm.cli.ExitCode;
import sonia.scm.cli.Table;
import sonia.scm.cli.TemplateRenderer;
import sonia.scm.group.Group;
import sonia.scm.template.TemplateEngineFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static java.util.Map.entry;

class GroupTemplateRenderer extends TemplateRenderer {

  private static final String NOT_FOUND_TEMPLATE = "{{i18n.groupNotFound}}\n";

  private static final String UNKNOWN_PERMISSION_TEMPLATE = "{{i18n.permissionUnknown}}: {{permission}}\n";
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

  void renderUnknownPermissionError(String permission) {
    renderToStderr(UNKNOWN_PERMISSION_TEMPLATE, Map.of("permission", permission));
    getContext().exit(ExitCode.USAGE);
  }

  void render(Collection<String> permissions) {
    renderToStdout(PERMISSION_LIST_TEMPLATE, Map.ofEntries(entry("permissions", permissions)));
  }
}
