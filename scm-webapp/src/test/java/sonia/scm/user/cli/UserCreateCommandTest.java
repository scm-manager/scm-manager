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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCreateCommandTest {

  private final UserTemplateTestRenderer testRenderer = new UserTemplateTestRenderer();

  @Mock
  private CommandValidator validator;
  @Mock
  private UserManager manager;
  @Mock
  private PasswordService passwordService;

  private UserCreateCommand command;

  @BeforeEach
  void initCommand() {
    command = new UserCreateCommand(testRenderer.getTemplateRenderer(), validator, manager, passwordService);
  }

  @Nested
  class ForSuccessfulCreationTest {
    private static final String PASSWORD = "patrician";
    private static final String ENC_PASSWORD = "enc_patrician";

    @BeforeEach
    void mockCreation() {
      when(manager.create(any()))
        .thenAnswer(invocation -> {
          User createdUser = invocation.getArgument(0, User.class);
          createdUser.setCreationDate(1649262000000L);
          return createdUser;
        });

      command.setUsername("havelock");
      command.setDisplayName("Havelock Vetinari");
      command.setEmail("havelock.vetinari@discworld");
      lenient().when(passwordService.encryptPassword(PASSWORD)).thenReturn(ENC_PASSWORD);
    }

    @Test
    void shouldCreateInternalUser() {
      command.setPassword("patrician");

      command.run();

      verify(manager).create(argThat(argument -> {
        assertThat(argument.getName()).isEqualTo("havelock");
        assertThat(argument.getDisplayName()).isEqualTo("Havelock Vetinari");
        assertThat(argument.isExternal()).isFalse();
        assertThat(argument.getPassword()).isEqualTo(ENC_PASSWORD);
        assertThat(argument.getMail()).isEqualTo("havelock.vetinari@discworld");
        assertThat(argument.isActive()).isTrue();
        return true;
      }));
    }

    @Test
    void shouldCreateExternalUser() {
      command.setExternal(true);

      command.run();

      verify(manager).create(argThat(argument -> {
        assertThat(argument.getName()).isEqualTo("havelock");
        assertThat(argument.getDisplayName()).isEqualTo("Havelock Vetinari");
        assertThat(argument.isExternal()).isTrue();
        assertThat(argument.getPassword()).isNull();
        assertThat(argument.getMail()).isEqualTo("havelock.vetinari@discworld");
        assertThat(argument.isActive()).isTrue();
        return true;
      }));
    }

    @Test
    void shouldCreateInactiveUser() {
      command.setPassword(PASSWORD);
      command.setInactive(true);

      command.run();

      verify(manager).create(argThat(argument -> {
        assertThat(argument.getName()).isEqualTo("havelock");
        assertThat(argument.getDisplayName()).isEqualTo("Havelock Vetinari");
        assertThat(argument.isExternal()).isFalse();
        assertThat(argument.getPassword()).isEqualTo(ENC_PASSWORD);
        assertThat(argument.getMail()).isEqualTo("havelock.vetinari@discworld");
        assertThat(argument.isActive()).isFalse();
        return true;
      }));
    }

    @Test
    void shouldPrintUserAfterCreationInEnglish() {
      testRenderer.setLocale("en");
      command.setPassword(PASSWORD);

      command.run();

      assertThat(testRenderer.getStdOut())
        .contains(
          "Username:      havelock",
          "Display Name:  Havelock Vetinari",
          "Email address: havelock.vetinari@discworld",
          "External:      no",
          "Active:        yes",
          "Creation Date: 2022-04-06T16:20:00Z",
          "Last Modified:"
        );
      assertThat(testRenderer.getStdErr()).isEmpty();
    }

    @Test
    void shouldPrintUserAfterCreationInGerman() {
      testRenderer.setLocale("de");
      command.setPassword(PASSWORD);

      command.run();

      assertThat(testRenderer.getStdOut())
        .contains(
          "Benutzername:       havelock",
          "Anzeigename:        Havelock Vetinari ",
          "E-Mail-Adresse:     havelock.vetinari@discworld",
          "Extern:             nein",
          "Aktiv:              ja",
          "Erstellt:           2022-04-06T16:20:00Z",
          "Zuletzt bearbeitet:"
        );
      assertThat(testRenderer.getStdErr()).isEmpty();
    }

    @Test
    void shouldEncryptPassword() {
      command.setPassword(PASSWORD);

      command.run();

      verify(manager).create(argThat(argument -> {
        assertThat(argument.getName()).isEqualTo("havelock");
        assertThat(argument.getDisplayName()).isEqualTo("Havelock Vetinari");
        assertThat(argument.isExternal()).isFalse();
        assertThat(argument.getPassword()).isEqualTo(ENC_PASSWORD);
        assertThat(argument.getMail()).isEqualTo("havelock.vetinari@discworld");
        assertThat(argument.isActive()).isTrue();
        return true;
      }));
    }
  }

  @Nested
  class ForUnsuccessfulCreationTest {

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
    void shouldFailWithEnglishMsgIfInternalUserWithoutPassword() {
      testRenderer.setLocale("en");

      assertThrows(CliExitException.class, () -> command.run());

      verifyNoInteractions(manager);
      assertThat(testRenderer.getStdOut()).isEmpty();
      assertThat(testRenderer.getStdErr()).contains("Valid password is required for internal users");
    }

    @Test
    void shouldFailWithGermanMsgIfInternalUserWithoutPassword() {
      testRenderer.setLocale("de");

      assertThrows(CliExitException.class, () -> command.run());

      verifyNoInteractions(manager);
      assertThat(testRenderer.getStdOut()).isEmpty();
      assertThat(testRenderer.getStdErr()).contains("Für interne Benutzer muss ein gültiges Passwort gesetzt werden");
    }

    @Test
    void shouldFailWithEnglishMsgIfExternalUserAndInactive() {
      testRenderer.setLocale("en");
      command.setPassword("123456");
      command.setExternal(true);
      command.setInactive(true);

      assertThrows(CliExitException.class, () -> command.run());

      verifyNoInteractions(manager);
      assertThat(testRenderer.getStdOut()).isEmpty();
      assertThat(testRenderer.getStdErr()).contains("External users cannot be deactivated");
    }

    @Test
    void shouldFailWithGermanMsgIfExternalUserWithInactive() {
      testRenderer.setLocale("de");
      command.setPassword("123456");
      command.setExternal(true);
      command.setInactive(true);

      assertThrows(CliExitException.class, () -> command.run());

      verifyNoInteractions(manager);
      assertThat(testRenderer.getStdOut()).isEmpty();
      assertThat(testRenderer.getStdErr()).contains("Externe Benutzer können nicht deaktiviert werden");
    }
  }
}
