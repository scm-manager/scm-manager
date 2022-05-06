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

package sonia.scm.cli;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class CommandRegistry {

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
