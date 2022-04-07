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
import sonia.scm.user.User;
import sonia.scm.user.UserManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDeleteCommandTest {

  private final UserTemplateTestRenderer testRenderer = new UserTemplateTestRenderer();

  @Mock
  private UserManager manager;

  private UserDeleteCommand command;

  @BeforeEach
  void initCommand() {
    command = new UserDeleteCommand(testRenderer.getTemplateRenderer(), manager);
  }

  @Nested
  class ForSuccessfulDeletionTest {

    private User user;

    @BeforeEach
    void mockGet() {
      user = new User();
      lenient().when(manager.get(any())).thenReturn(user);
    }

    @Test
    void shouldRenderPromptInEnglishWithoutYesFlag() {
      testRenderer.setLocale("en");

      command.run();

      verifyNoInteractions(manager);
      assertThat(testRenderer.getStdOut()).isEmpty();
      assertThat(testRenderer.getStdErr()).contains("If you really want to delete this user please pass --yes");
    }

    @Test
    void shouldRenderPromptInGermanWithoutYesFlag() {
      testRenderer.setLocale("de");

      command.run();

      verifyNoInteractions(manager);
      assertThat(testRenderer.getStdOut()).isEmpty();
      assertThat(testRenderer.getStdErr()).contains("Wenn dieser Benutzer endgültig gelöscht werden soll, setzen Sie bitte --yes");
    }

    @Test
    void shouldDeleteUserWithEnglishMsg() {
      testRenderer.setLocale("en");
      command.setShouldDelete(true);

      command.run();

      verify(manager).delete(user);
      assertThat(testRenderer.getStdOut()).contains("Successfully deleted user");
      assertThat(testRenderer.getStdErr()).isEmpty();
    }

    @Test
    void shouldDeleteUserWithGermanMsg() {
      testRenderer.setLocale("de");
      command.setShouldDelete(true);

      command.run();

      verify(manager).delete(user);
      assertThat(testRenderer.getStdOut()).contains("Benutzer erfolgreich gelöscht");
      assertThat(testRenderer.getStdErr()).isEmpty();
    }
  }

  @Nested
  class ForUnsuccessfulDeletionTest {

    @Test
    void shouldFailSilentlyIfUserNotFound() {
      when(manager.get(any())).thenReturn(null);
      command.setShouldDelete(true);

      command.run();

      verify(manager, never()).delete(any());
      assertThat(testRenderer.getStdOut()).isEmpty();
      assertThat(testRenderer.getStdErr()).isEmpty();
    }
  }
}
