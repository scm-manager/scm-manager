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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.cli.CliExitException;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDeactivateCommandTest {

  private final UserTemplateTestRenderer testRenderer = new UserTemplateTestRenderer();

  @Mock
  private UserManager manager;

  private UserDeactivateCommand command;

  @BeforeEach
  void initCommand() {
    command = new UserDeactivateCommand(testRenderer.getTemplateRenderer(), manager);
  }

  @Nested
  class ForSuccessfulDeactivationTest {

    @BeforeEach
    void mockGet() {
      User user = new User("havelock", "Havelock Vetinari", "havelock.vetinari@discworld");
      user.setPassword("patrician");
      user.setExternal(false);
      user.setActive(true);
      user.setCreationDate(1649262000000L);
      user.setLastModified(1649272000000L);
      when(manager.get(any())).thenReturn(user);
    }

    @Test
    void shouldDeactivateInternalUser() {
      command.run();

      verify(manager).modify(argThat(argument -> {
        assertThat(argument.getName()).isEqualTo("havelock");
        assertThat(argument.getDisplayName()).isEqualTo("Havelock Vetinari");
        assertThat(argument.isExternal()).isFalse();
        assertThat(argument.getPassword()).isEqualTo("patrician");
        assertThat(argument.getMail()).isEqualTo("havelock.vetinari@discworld");
        assertThat(argument.isActive()).isFalse();
        return true;
      }));
    }

    @Test
    void shouldPrintUserAfterDeactivationInEnglish() {
      testRenderer.setLocale("en");

      command.run();

      assertThat(testRenderer.getStdOut())
        .contains(
          "Username:      havelock",
          "Display Name:  Havelock Vetinari",
          "Email address: havelock.vetinari@discworld",
          "External:      no",
          "Active:        no",
          "Creation Date: 2022-04-06T16:20:00Z",
          "Last Modified: 2022-04-06T19:06:40Z"
        );
      assertThat(testRenderer.getStdErr()).isEmpty();
    }

    @Test
    void shouldPrintUserAfterDeactivationInGerman() {
      testRenderer.setLocale("de");

      command.run();

      assertThat(testRenderer.getStdOut())
        .contains(
          "Benutzername:       havelock",
          "Anzeigename:        Havelock Vetinari ",
          "E-Mail-Adresse:     havelock.vetinari@discworld",
          "Extern:             nein",
          "Aktiv:              nein",
          "Erstellt:           2022-04-06T16:20:00Z",
          "Zuletzt bearbeitet: 2022-04-06T19:06:40Z"
        );
      assertThat(testRenderer.getStdErr()).isEmpty();
    }
  }

  @Nested
  class ForUnsuccessfulActivationTest {

    @Test
    void shouldFailWithEnglishMsgIfUserNotFound() {
      testRenderer.setLocale("en");
      when(manager.get(any())).thenReturn(null);

      assertThrows(CliExitException.class, () -> command.run());

      verify(manager, never()).modify(any());
      assertThat(testRenderer.getStdOut()).isEmpty();
      assertThat(testRenderer.getStdErr()).contains("Could not find user");
    }

    @Test
    void shouldFailWithGermanMsgIfUserNotFound() {
      testRenderer.setLocale("de");
      when(manager.get(any())).thenReturn(null);

      assertThrows(CliExitException.class, () -> command.run());

      verify(manager, never()).modify(any());
      assertThat(testRenderer.getStdOut()).isEmpty();
      assertThat(testRenderer.getStdErr()).contains("Benutzer konnte nicht gefunden werden");
    }

    @Nested
    class ForExternalUserTest {

      @BeforeEach
      void mockExternalUser() {
        User user = new User();
        user.setExternal(true);
        when(manager.get(any())).thenReturn(user);
      }

      @Test
      void shouldFailWithEnglishMsgIfUserIsExternal() {
        testRenderer.setLocale("en");

        assertThrows(CliExitException.class, () -> command.run());

        verify(manager, never()).modify(any());
        assertThat(testRenderer.getStdOut()).isEmpty();
        assertThat(testRenderer.getStdErr()).contains("External users cannot be deactivated");
      }

      @Test
      void shouldFailWithGermanMsgIfUserIsExternal() {
        testRenderer.setLocale("de");

        assertThrows(CliExitException.class, () -> command.run());

        verify(manager, never()).modify(any());
        assertThat(testRenderer.getStdOut()).isEmpty();
        assertThat(testRenderer.getStdErr()).contains("Externe Benutzer können nicht deaktiviert werden");
      }
    }
  }
}
