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

import org.apache.shiro.authc.credential.PasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.cli.CliExitException;
import sonia.scm.cli.CommandValidator;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserModifyCommandTest {

  private final UserTemplateTestRenderer testRenderer = new UserTemplateTestRenderer();

  @Mock
  private CommandValidator validator;
  @Mock
  private UserManager manager;
  @Mock
  private PasswordService passwordService;

  private UserModifyCommand command;

  @BeforeEach
  void initCommand() {
    command = new UserModifyCommand(testRenderer.getTemplateRenderer(), validator, manager, passwordService);
  }

  @Nested
  class ForSuccessfulModificationTest {
    private static final String NEW_PASSWORD = "havelock";
    private static final String NEW_ENC_PASSWORD = "enc_havelock";

    @BeforeEach
    void mockGet() {
      User user = new User("havelock", "Havelock Vetinari", "havelock.vetinari@discworld");
      user.setPassword("patrician");
      user.setExternal(false);
      user.setActive(true);
      user.setCreationDate(1649262000000L);
      user.setLastModified(1649272000000L);
      when(manager.get(any())).thenReturn(user);
      lenient().when(passwordService.encryptPassword(NEW_PASSWORD)).thenReturn(NEW_ENC_PASSWORD);
    }

    @Test
    void shouldModifyUser() {
      command.setDisplayName("Lord Vetinari");
      command.setEmail("patrician@discworld");
      command.setPassword(NEW_PASSWORD);

      command.run();

      verify(manager).modify(argThat(argument -> {
        assertThat(argument.getName()).isEqualTo("havelock");
        assertThat(argument.getDisplayName()).isEqualTo("Lord Vetinari");
        assertThat(argument.isExternal()).isFalse();
        assertThat(argument.getPassword()).isEqualTo(NEW_ENC_PASSWORD);
        assertThat(argument.getMail()).isEqualTo("patrician@discworld");
        assertThat(argument.isActive()).isTrue();
        return true;
      }));
    }

    @Test
    void shouldNotModifyDisplayNameIfNotSet() {
      command.setEmail("patrician@discworld");
      command.setPassword(NEW_PASSWORD);

      command.run();

      verify(manager).modify(argThat(argument -> {
        assertThat(argument.getName()).isEqualTo("havelock");
        assertThat(argument.getDisplayName()).isEqualTo("Havelock Vetinari");
        assertThat(argument.isExternal()).isFalse();
        assertThat(argument.getPassword()).isEqualTo(NEW_ENC_PASSWORD);
        assertThat(argument.getMail()).isEqualTo("patrician@discworld");
        assertThat(argument.isActive()).isTrue();
        return true;
      }));
    }

    @Test
    void shouldNotModifyEmailIfNotSet() {
      command.setDisplayName("Lord Vetinari");
      command.setPassword(NEW_PASSWORD);

      command.run();

      verify(manager).modify(argThat(argument -> {
        assertThat(argument.getName()).isEqualTo("havelock");
        assertThat(argument.getDisplayName()).isEqualTo("Lord Vetinari");
        assertThat(argument.isExternal()).isFalse();
        assertThat(argument.getPassword()).isEqualTo(NEW_ENC_PASSWORD);
        assertThat(argument.getMail()).isEqualTo("havelock.vetinari@discworld");
        assertThat(argument.isActive()).isTrue();
        return true;
      }));
    }

    @Test
    void shouldNotModifyPasswordIfNotSet() {
      command.setEmail("patrician@discworld");
      command.setDisplayName("Lord Vetinari");

      command.run();

      verify(manager).modify(argThat(argument -> {
        assertThat(argument.getName()).isEqualTo("havelock");
        assertThat(argument.getDisplayName()).isEqualTo("Lord Vetinari");
        assertThat(argument.isExternal()).isFalse();
        assertThat(argument.getPassword()).isEqualTo("patrician");
        assertThat(argument.getMail()).isEqualTo("patrician@discworld");
        assertThat(argument.isActive()).isTrue();
        return true;
      }));
    }

    @Test
    void shouldPrintUserAfterModificationInEnglish() {
      testRenderer.setLocale("en");
      command.setPassword("havelock");
      command.setDisplayName("Lord Vetinari");
      command.setEmail("patrician@discworld");

      command.run();

      assertThat(testRenderer.getStdOut())
        .contains(
          "Username:      havelock",
          "Display Name:  Lord Vetinari",
          "Email address: patrician@discworld",
          "External:      no",
          "Active:        yes",
          "Creation Date: 2022-04-06T16:20:00Z",
          "Last Modified: 2022-04-06T19:06:40Z"
        );
      assertThat(testRenderer.getStdErr()).isEmpty();
    }

    @Test
    void shouldPrintUserAfterModificationInGerman() {
      testRenderer.setLocale("de");
      command.setPassword("havelock");
      command.setDisplayName("Lord Vetinari");
      command.setEmail("patrician@discworld");

      command.run();

      assertThat(testRenderer.getStdOut())
        .contains(
          "Benutzername:       havelock",
          "Anzeigename:        Lord Vetinari ",
          "E-Mail-Adresse:     patrician@discworld",
          "Extern:             nein",
          "Aktiv:              ja",
          "Erstellt:           2022-04-06T16:20:00Z",
          "Zuletzt bearbeitet: 2022-04-06T19:06:40Z"
        );
      assertThat(testRenderer.getStdErr()).isEmpty();
    }
  }

  @Nested
  class ForUnsuccessfulModificationTest {

    @Test
    void shouldFailIfValidatorFails() {
      doThrow(picocli.CommandLine.ParameterException.class).when(validator).validate();

      assertThrows(
        picocli.CommandLine.ParameterException.class,
        () -> command.run()
      );

      assertThat(testRenderer.getStdOut()).isEmpty();
    }

    @Test
    void shouldFailWithEnglishMsgIfUserNotFound() {
      testRenderer.setLocale("en");
      command.setPassword("havelock");
      when(manager.get(any())).thenReturn(null);

      assertThrows(CliExitException.class, () -> command.run());

      verify(manager, never()).modify(any());
      assertThat(testRenderer.getStdOut()).isEmpty();
      assertThat(testRenderer.getStdErr()).contains("Could not find user");
    }

    @Test
    void shouldFailWithGermanMsgIfUserNotFound() {
      testRenderer.setLocale("de");
      command.setPassword("havelock");
      when(manager.get(any())).thenReturn(null);

      assertThrows(CliExitException.class, () -> command.run());

      verify(manager, never()).modify(any());
      assertThat(testRenderer.getStdOut()).isEmpty();
      assertThat(testRenderer.getStdErr()).contains("Benutzer konnte nicht gefunden werden");
    }
  }
}
