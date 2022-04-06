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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.cli.CommandValidator;
import sonia.scm.cli.TemplateTestRenderer;
import sonia.scm.group.Group;
import sonia.scm.group.GroupManager;

import java.util.ResourceBundle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupCreateCommandTest {

  private final TemplateTestRenderer testRenderer = new TemplateTestRenderer();
  private final GroupTemplateRenderer templateRenderer = new GroupTemplateRenderer(testRenderer.getContextMock(), testRenderer.getTemplateEngineFactory()) {
    @Override
    protected ResourceBundle getBundle() {
      return testRenderer.getResourceBundle();
    }
  };
  private final GroupCommandBeanMapper beanMapper = new GroupCommandBeanMapperImpl();

  @Mock
  private CommandValidator validator;
  @Mock
  private GroupManager manager;

  private GroupCreateCommand command;

  @BeforeEach
  void initCommand() {
    command = new GroupCreateCommand(templateRenderer, validator, manager, beanMapper);
  }

  @Nested
  class ForSuccessfulCreationTest {

    @BeforeEach
    void mockCreation() {
      when(manager.create(any()))
        .thenAnswer(invocation -> {
          Group createdGroup = invocation.getArgument(0, Group.class);
          createdGroup.setCreationDate(1649262000000L);
          return createdGroup;
        });

      command.setName("hog");
      command.setDescription("Crew of the Heart of Gold");
      command.setMembers(new String[]{"zaphod", "trillian"});
    }

    @Test
    void shouldCreateGroup() {
      command.run();

      verify(manager).create(argThat(argument -> {
        assertThat(argument.getName()).isEqualTo("hog");
        assertThat(argument.getDescription()).isEqualTo("Crew of the Heart of Gold");
        assertThat(argument.getMembers()).contains("zaphod", "trillian");
        return true;
      }));
    }

    @Test
    void shouldPrintGroupAfterCreation() {
      command.run();

      assertThat(testRenderer.getStdOut())
        .contains(
          "groupName:         hog",
          "groupDescription:  Crew of the Heart of Gold",
          "groupMembers:      zaphod, trillian",
          "groupExternal:     no",
          "groupCreationDate: 2022-04-06T16:20:00Z",
          "groupLastModified:"
        );
      assertThat(testRenderer.getStdErr())
        .isEmpty();
    }
  }

  @Test
  void shouldFailIfValidatorFails() {
    doThrow(picocli.CommandLine.ParameterException.class).when(validator).validate();

    Assertions.assertThrows(
      picocli.CommandLine.ParameterException.class,
      () -> command.run()
    );

    assertThat(testRenderer.getStdOut())
      .isEmpty();
  }
}
