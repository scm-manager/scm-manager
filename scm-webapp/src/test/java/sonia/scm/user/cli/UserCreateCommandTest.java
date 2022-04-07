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

package sonia.scm.user.cli;

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

  private UserCreateCommand command;

  @BeforeEach
  void initCommand() {
    command = new UserCreateCommand(testRenderer.getTemplateRenderer(), validator, manager);
  }

  @Nested
  class ForSuccessfulCreationTest {

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
    }

    @Test
    void shouldCreateInternalUser() {
      command.setPassword("patrician");

      command.run();

      verify(manager).create(argThat(argument -> {
        assertThat(argument.getName()).isEqualTo("havelock");
        assertThat(argument.getDisplayName()).isEqualTo("Havelock Vetinari");
        assertThat(argument.isExternal()).isFalse();
        assertThat(argument.getPassword()).isEqualTo("patrician");
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
      command.setPassword("patrician");
      command.setInactive(true);

      command.run();

      verify(manager).create(argThat(argument -> {
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
    void shouldPrintUserAfterCreationInEnglish() {
      testRenderer.setLocale("en");
      command.setPassword("patrician");

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
      command.setPassword("patrician");

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
      assertThat(testRenderer.getStdErr()).contains("Password is required for internal users");
    }

    @Test
    void shouldFailWithGermanMsgIfInternalUserWithoutPassword() {
      testRenderer.setLocale("de");

      assertThrows(CliExitException.class, () -> command.run());

      verifyNoInteractions(manager);
      assertThat(testRenderer.getStdOut()).isEmpty();
      assertThat(testRenderer.getStdErr()).contains("Für interne Benutzer muss ein Passwort gesetzt werden");
    }

    @Test
    void shouldFailWithEnglishMsgIfExternalUserAndInactive() {
      testRenderer.setLocale("en");
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
      command.setExternal(true);
      command.setInactive(true);

      assertThrows(CliExitException.class, () -> command.run());

      verifyNoInteractions(manager);
      assertThat(testRenderer.getStdOut()).isEmpty();
      assertThat(testRenderer.getStdErr()).contains("Externe Benutzer können nicht deaktiviert werden");
    }
  }
}
