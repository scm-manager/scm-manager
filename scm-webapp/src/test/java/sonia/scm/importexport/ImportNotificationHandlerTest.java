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

package sonia.scm.importexport;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.notifications.NotificationSender;
import sonia.scm.notifications.Type;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.PullResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ImportNotificationHandlerTest {

  @Mock
  private NotificationSender notificationSender;
  @InjectMocks
  private ImportNotificationHandler handler;

  private final Repository repository = RepositoryTestData.createHeartOfGold();

  @Test
  void shouldCreateSuccessNotification() {
    handler.handleSuccessfulImport(repository);

    verify(notificationSender).send(argThat(
      notification -> {
        assertThat(notification.getType()).isEqualTo(Type.SUCCESS);
        assertThat(notification.getMessage()).isEqualTo("importFinished");
        assertThat(notification.getParameters()).isNull();
        return true;
      }
    ));
  }

  @Test
  void shouldCreateSuccessNotificationWithLfs() {
    handler.handleSuccessfulImport(repository, new PullResponse.LfsCount(42, 0));

    verify(notificationSender).send(argThat(
      notification -> {
        assertThat(notification.getType()).isEqualTo(Type.SUCCESS);
        assertThat(notification.getMessage()).isEqualTo("importWithLfsFinished");
        assertThat(notification.getParameters()).containsEntry("successCount", "42");
        return true;
      }
    ));
  }

  @Test
  void shouldCreateFailureNotification() {
    handler.handleFailedImport();

    verify(notificationSender).send(argThat(
      notification -> {
        assertThat(notification.getType()).isEqualTo(Type.ERROR);
        assertThat(notification.getMessage()).isEqualTo("importFailed");
        assertThat(notification.getParameters()).isNull();
        return true;
      }
    ));
  }

  @Test
  void shouldCreateLfsFailureNotification() {
    handler.handleSuccessfulImportWithLfsFailures(repository, new PullResponse.LfsCount(42, 7));

    verify(notificationSender).send(argThat(
      notification -> {
        assertThat(notification.getType()).isEqualTo(Type.ERROR);
        assertThat(notification.getMessage()).isEqualTo("importLfsFailed");
        assertThat(notification.getParameters())
          .containsEntry("successCount", "42")
          .containsEntry("failureCount", "7")
          .containsEntry("overallCount", "49");
        return true;
      }
    ));
  }
}
