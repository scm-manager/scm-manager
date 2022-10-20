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
