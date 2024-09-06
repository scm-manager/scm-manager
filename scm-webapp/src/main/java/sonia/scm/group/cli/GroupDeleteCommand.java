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

import com.google.common.annotations.VisibleForTesting;
import jakarta.inject.Inject;
import picocli.CommandLine;
import sonia.scm.cli.ParentCommand;
import sonia.scm.group.Group;
import sonia.scm.group.GroupManager;

import java.util.Collections;

@CommandLine.Command(name = "delete", aliases = "rm")
@ParentCommand(GroupCommand.class)
class GroupDeleteCommand implements Runnable {

  private static final String PROMPT_TEMPLATE = "{{i18n.groupDeletePrompt}}";

  @CommandLine.Parameters(descriptionKey = "scm.group.delete.group", paramLabel = "name")
  private String name;

  @CommandLine.Option(names = {"--yes", "-y"}, descriptionKey = "scm.group.delete.prompt")
  private boolean shouldDelete;

  @CommandLine.Mixin
  private final GroupTemplateRenderer templateRenderer;
  private final GroupManager manager;

  @Inject
  GroupDeleteCommand(GroupTemplateRenderer templateRenderer, GroupManager manager) {
    this.templateRenderer = templateRenderer;
    this.manager = manager;
  }

  @Override
  public void run() {
    if (!shouldDelete) {
      templateRenderer.renderToStderr(PROMPT_TEMPLATE, Collections.emptyMap());
      return;
    }
    Group group = manager.get(name);
    if (group != null) {
      manager.delete(group);
    }
  }

  @VisibleForTesting
  void setName(String name) {
    this.name = name;
  }

  @VisibleForTesting
  void setShouldDelete(boolean shouldDelete) {
    this.shouldDelete = shouldDelete;
  }
}
