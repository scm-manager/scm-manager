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
