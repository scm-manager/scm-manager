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
import sonia.scm.repository.RepositoryTestData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExportNotificationHandlerTest {

  @Mock
  private NotificationSender sender;

  @InjectMocks
  private ExportNotificationHandler handler;

  @Test
  void shouldSendFailedNotification() {
    handler.handleFailedExport(RepositoryTestData.create42Puzzle());

    verify(sender).send(argThat(notification -> {
      assertThat(notification.getType()).isEqualTo(Type.ERROR);
      assertThat(notification.getLink()).isEqualTo("/repo/hitchhiker/42Puzzle/settings/general");
      assertThat(notification.getMessage()).isEqualTo("exportFailed");
      return true;
    }));
  }

  @Test
  void shouldSendSuccessfulNotification() {
    handler.handleSuccessfulExport(RepositoryTestData.create42Puzzle());

    verify(sender).send(argThat(notification -> {
      assertThat(notification.getType()).isEqualTo(Type.SUCCESS);
      assertThat(notification.getLink()).isEqualTo("/repo/hitchhiker/42Puzzle/settings/general");
      assertThat(notification.getMessage()).isEqualTo("exportFinished");
      return true;
    }));
  }

}
