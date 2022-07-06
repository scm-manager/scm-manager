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
class UserConvertToInternalCommandTest {

  private final UserTemplateTestRenderer testRenderer = new UserTemplateTestRenderer();

  @Mock
  private UserManager manager;

  private UserConvertToInternalCommand command;

  @BeforeEach
  void initCommand() {
    command = new UserConvertToInternalCommand(testRenderer.getTemplateRenderer(), manager);
  }

  @Nested
  class ForSuccessfulActivationTest {

    @BeforeEach
    void mockGet() {
      User user = new User("havelock", "Havelock Vetinari", "havelock.vetinari@discworld");
      user.setPassword("patrician");
      user.setExternal(true);
      user.setActive(true);
      user.setCreationDate(1649262000000L);
      user.setLastModified(1649272000000L);
      when(manager.get(any())).thenReturn(user);
    }

    @Test
    void shouldActivateInternalUser() {
      command.setPassword("havelock123");

      command.run();

      verify(manager).modify(argThat(argument -> {
        assertThat(argument.getName()).isEqualTo("havelock");
        assertThat(argument.getDisplayName()).isEqualTo("Havelock Vetinari");
        assertThat(argument.isExternal()).isFalse();
        assertThat(argument.getPassword()).isEqualTo("havelock123");
        assertThat(argument.getMail()).isEqualTo("havelock.vetinari@discworld");
        assertThat(argument.isActive()).isTrue();
        return true;
      }));
    }

    @Test
    void shouldPrintUserAfterActivationInEnglish() {
      testRenderer.setLocale("en");
      command.setPassword("havelock123");

      command.run();

      assertThat(testRenderer.getStdOut())
        .contains(
          "Username:      havelock",
          "Display Name:  Havelock Vetinari",
          "Email address: havelock.vetinari@discworld",
          "External:      no",
          "Active:        yes",
          "Creation Date: 2022-04-06T16:20:00Z",
          "Last Modified: 2022-04-06T19:06:40Z"
        );
      assertThat(testRenderer.getStdErr()).isEmpty();
    }

    @Test
    void shouldPrintUserAfterActivationInGerman() {
      testRenderer.setLocale("de");
      command.setPassword("havelock123");

      command.run();

      assertThat(testRenderer.getStdOut())
        .contains(
          "Benutzername:       havelock",
          "Anzeigename:        Havelock Vetinari ",
          "E-Mail-Adresse:     havelock.vetinari@discworld",
          "Extern:             nein",
          "Aktiv:              ja",
          "Erstellt:           2022-04-06T16:20:00Z",
          "Zuletzt bearbeitet: 2022-04-06T19:06:40Z"
        );
      assertThat(testRenderer.getStdErr()).isEmpty();
    }
  }

  @Nested
  class ForUnsuccessfulConversionTest {

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
  }
}
