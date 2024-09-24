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

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class CommandRegistryTest {

  @Mock
  private RegisteredCommandCollector commandCollector;
  @InjectMocks
  private CommandRegistry registry;

  @Test
  void shouldCreateTreeWithOnlyRootNodes() {
    mockCommands(rc(Object.class), rc(String.class), rc(Integer.class));

    List<RegisteredCommandNode> commandTree = registry.createCommandTree();
    assertContainsCommands(commandTree, Object.class, String.class, Integer.class);
  }

  @Test
  void shouldCreateTreeWithParents() {
    mockCommands(rc(Object.class), rc(String.class, Object.class), rc(Integer.class, Object.class));

    List<RegisteredCommandNode> commandTree = registry.createCommandTree();

    assertContainsCommands(commandTree, Object.class);
    assertContainsCommands(commandTree.iterator().next().getChildren(), Integer.class, String.class);
  }

  @Test
  void shouldCreateTreeWithParentsSecondLevel() {
    mockCommands(rc(Object.class), rc(String.class, Object.class), rc(Integer.class, String.class));

    List<RegisteredCommandNode> commandTree = registry.createCommandTree();

    assertContainsCommands(commandTree, Object.class);
    RegisteredCommandNode rootNode = commandTree.iterator().next();
    assertContainsCommands(rootNode.getChildren(), String.class);
    assertContainsCommands(rootNode.getChildren().get(0).getChildren(), Integer.class);
  }

  @Test
  void shouldSortCommandsAlphabetically() {
    mockCommands(rc(Object.class), rc(String.class, Object.class), rc(Float.class, Object.class), rc(Integer.class, Object.class));

    List<RegisteredCommandNode> commandTree = registry.createCommandTree();

    List<RegisteredCommandNode> subCommands = commandTree.get(0).getChildren();
    assertThat(subCommands.get(0).getCommand()).isEqualTo(Float.class);
    assertThat(subCommands.get(1).getCommand()).isEqualTo(Integer.class);
    assertThat(subCommands.get(2).getCommand()).isEqualTo(String.class);
  }

  private void mockCommands(RegisteredCommand... commands) {
    when(commandCollector.collect()).thenReturn(ImmutableSet.copyOf(commands));
  }

  private RegisteredCommand rc(Class<?> command) {
    return rc(command, null);
  }

  private RegisteredCommand rc(Class<?> command, Class<?> parent) {
    return new RegisteredCommand(command.getSimpleName(), command, parent);
  }

  private void assertContainsCommands(Collection<RegisteredCommandNode> nodes, Class... expected) {
    assertThat(nodes).map(RegisteredCommandNode::getCommand).contains(expected);
  }
}
