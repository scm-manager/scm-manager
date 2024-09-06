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

package sonia.scm.user.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.cli.TemplateTestRenderer;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserListCommandTest {

  @Mock
  private UserManager manager;

  private final UserCommandBeanMapper beanMapper = new UserCommandBeanMapperImpl();
  private final TemplateTestRenderer testRenderer = new TemplateTestRenderer();

  private UserListCommand command;

  @BeforeEach
  void initCommand() {
    command = new UserListCommand(testRenderer.createTemplateRenderer(), manager, beanMapper);
  }

  @BeforeEach
  void mockGroups() {
    User internalUser = new User("havelock", "Havelock Vetinari", "havelock.vetinari@discworld");
    User externalUser = new User("mustrum", "Mustrum Ridcully", "mustrum.ridcully@discworld");
    externalUser.setExternal(true);
    externalUser.setActive(false);
    when(manager.getAll())
      .thenReturn(
        asList(
          internalUser,
          externalUser));
  }

  @Test
  void shouldRenderShortTableInEnglish() {
    testRenderer.setLocale("en");
    command.setSpec(testRenderer.getMockedSpec());
    command.setUseShortTemplate(true);

    command.run();

    assertThat(testRenderer.getStdOut()).isEqualTo("havelock\nmustrum\n");
    assertThat(testRenderer.getStdErr()).isEmpty();
  }

  @Test
  void shouldRenderShortTableInGerman() {
    testRenderer.setLocale("de");
    command.setSpec(testRenderer.getMockedSpec());
    command.setUseShortTemplate(true);

    command.run();

    assertThat(testRenderer.getStdOut()).isEqualTo("havelock\nmustrum\n");
    assertThat(testRenderer.getStdErr()).isEmpty();
  }

  @Test
  void shouldRenderLongTableInEnglish() {
    testRenderer.setLocale("en");
    command.setSpec(testRenderer.getMockedSpec());

    command.run();

    assertThat(testRenderer.getStdOut())
      .isEqualTo("USERNAME DISPLAY NAME      EMAIL ADDRESS               EXTERNAL ACTIVE\n" +
        "havelock Havelock Vetinari havelock.vetinari@discworld no       yes   \n" +
        "mustrum  Mustrum Ridcully  mustrum.ridcully@discworld  yes      no    \n");
    assertThat(testRenderer.getStdErr()).isEmpty();
  }

  @Test
  void shouldRenderLongTableInGerman() {
    testRenderer.setLocale("de");
    command.setSpec(testRenderer.getMockedSpec());

    command.run();

    assertThat(testRenderer.getStdOut())
      .isEqualTo("BENUTZERNAME ANZEIGENAME       E-MAIL-ADRESSE              EXTERN AKTIV\n" +
        "havelock     Havelock Vetinari havelock.vetinari@discworld nein   ja   \n" +
        "mustrum      Mustrum Ridcully  mustrum.ridcully@discworld  ja     nein \n");
    assertThat(testRenderer.getStdErr()).isEmpty();
  }
}
