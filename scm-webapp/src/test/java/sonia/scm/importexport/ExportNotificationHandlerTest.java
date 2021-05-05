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
