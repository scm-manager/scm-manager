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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Singleton
public class CommandRegistry {

  private static final Logger LOG = LoggerFactory.getLogger(CommandRegistry.class);
  private final RegisteredCommandCollector commandCollector;

  @Inject
  public CommandRegistry(RegisteredCommandCollector commandCollector) {
    this.commandCollector = commandCollector;
  }

  public Set<RegisteredCommandNode> createCommandTree() {
    Set<RegisteredCommandNode> rootCommands = new HashSet<>();
    Set<RegisteredCommand> registeredCommands = commandCollector.collect();

    Map<Class<?>, RegisteredCommandNode> commandNodes = new HashMap<>();

    for (RegisteredCommand command : registeredCommands) {
      commandNodes.put(command.getCommand(), new RegisteredCommandNode(command.getName(), command.getCommand()));
    }

    for (RegisteredCommand command : registeredCommands) {
      RegisteredCommandNode node = commandNodes.get(command.getCommand());
      if (command.getParent() == null) {
        rootCommands.add(node);
      } else {
        RegisteredCommandNode parentNode = commandNodes.get(command.getParent());
        if (parentNode != null) {
          parentNode.getChildren().add(node);
        } else {
          //TODO Handle
          LOG.warn("Could not find parent command");
        }
      }
    }
    return rootCommands;
  }

}
