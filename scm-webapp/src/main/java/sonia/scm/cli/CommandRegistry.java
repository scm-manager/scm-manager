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

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
class CommandRegistry {

  private final RegisteredCommandCollector commandCollector;

  @Inject
  public CommandRegistry(RegisteredCommandCollector commandCollector) {
    this.commandCollector = commandCollector;
  }

  public List<RegisteredCommandNode> createCommandTree() {
    List<RegisteredCommandNode> rootCommands = new ArrayList<>();
    List<RegisteredCommand> sortedCommands = collectSortedCommands();

    Map<Class<?>, RegisteredCommandNode> commandNodes = new HashMap<>();

    for (RegisteredCommand command : sortedCommands) {
      commandNodes.put(command.getCommand(), new RegisteredCommandNode(command.getName(), command.getCommand()));
    }

    for (RegisteredCommand command : sortedCommands) {
      RegisteredCommandNode node = commandNodes.get(command.getCommand());
      if (command.getParent() == null) {
        rootCommands.add(node);
      } else {
        RegisteredCommandNode parentNode = commandNodes.get(command.getParent());
        if (parentNode != null) {
          parentNode.getChildren().add(node);
        } else {
          throw new NonExistingParentCommandException("parent command of " + command.getName() + " does not exist");
        }
      }
    }
    return rootCommands;
  }

  private List<RegisteredCommand> collectSortedCommands() {
    return commandCollector.collect()
      .stream()
      .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
      .collect(Collectors.toList());
  }
}
