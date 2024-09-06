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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.cli.TemplateTestRenderer;
import sonia.scm.group.Group;
import sonia.scm.group.GroupManager;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupListCommandTest {

  @Mock
  private GroupManager manager;

  private final GroupCommandBeanMapper beanMapper = new GroupCommandBeanMapperImpl();
  private final TemplateTestRenderer testRenderer = new TemplateTestRenderer();

  private GroupListCommand command;

  @BeforeEach
  void initCommand() {
    command = new GroupListCommand(testRenderer.createTemplateRenderer(), manager, beanMapper);
    command.setSpec(testRenderer.getMockedSpec());
  }

  @BeforeEach
  void mockGroups() {
    Group internalGroup = new Group("test", "hog");
    Group externalGroup = new Group("test", "vogons");
    externalGroup.setExternal(true);
    when(manager.getAll())
      .thenReturn(
        asList(
          internalGroup,
          externalGroup));
  }

  @Test
  void shouldRenderShortTable() {
    command.setUseShortTemplate(true);

    command.run();

    assertThat(testRenderer.getStdOut()).isEqualTo("hog\nvogons\n");
  }

  @Test
  void shouldRenderLongTable() {
    command.run();

    assertThat(testRenderer.getStdOut())
      .isEqualTo("NAME   EXTERNAL\nhog    no      \nvogons yes     \n");
  }
}
