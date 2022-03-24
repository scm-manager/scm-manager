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

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

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

    Set<RegisteredCommandNode> commandTree = registry.createCommandTree();
    assertContainsCommands(commandTree, Object.class, String.class, Integer.class);
  }

  @Test
  void shouldCreateTreeWithParents() {
    mockCommands(rc(Object.class), rc(String.class, Object.class), rc(Integer.class, Object.class));

    Set<RegisteredCommandNode> commandTree = registry.createCommandTree();

    assertContainsCommands(commandTree, Object.class);
    assertContainsCommands(commandTree.iterator().next().getChildren(), Integer.class, String.class);
  }

  @Test
  void shouldCreateTreeWithParentsSecondLevel() {
    mockCommands(rc(Object.class), rc(String.class, Object.class), rc(Integer.class, String.class));

    Set<RegisteredCommandNode> commandTree = registry.createCommandTree();

    assertContainsCommands(commandTree, Object.class);
    RegisteredCommandNode rootNode = commandTree.iterator().next();
    assertContainsCommands(rootNode.getChildren(), String.class);
    assertContainsCommands(rootNode.getChildren().get(0).getChildren(), Integer.class);
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
