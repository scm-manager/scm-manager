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

import sonia.scm.notifications.Notification;
import sonia.scm.notifications.NotificationSender;
import sonia.scm.notifications.Type;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.PullResponse;

import javax.inject.Inject;
import java.util.Map;

class ImportNotificationHandler {

  private final NotificationSender notificationSender;

  @Inject
  ImportNotificationHandler(NotificationSender notificationSender) {
    this.notificationSender = notificationSender;
  }

  void handleSuccessfulImport(Repository repository) {
    handleSuccessfulImport(repository, new PullResponse.LfsCount(0, 0));
  }

  void handleSuccessfulImport(Repository repository, PullResponse.LfsCount lfsCount) {
    notificationSender.send(getImportSuccessfulNotification(repository, lfsCount));
  }

  void handleSuccessfulImportWithLfsFailures(Repository repository, PullResponse.LfsCount lfsCount) {
    notificationSender.send(getImportLfsFailedNotification(repository, lfsCount));
  }

  void handleFailedImport() {
    notificationSender.send(getImportFailedNotification());
  }

  private Notification getImportSuccessfulNotification(Repository repository, PullResponse.LfsCount lfsCount) {
    if (lfsCount.getSuccessCount() > 0 || lfsCount.getFailureCount() > 0) {
      return new Notification(Type.SUCCESS, createLink(repository), "importWithLfsFinished", createParameters(lfsCount));
    } else {
      return new Notification(Type.SUCCESS, createLink(repository), "importFinished");
    }
  }

  private Notification getImportLfsFailedNotification(Repository repository, PullResponse.LfsCount lfsCount) {
    return new Notification(Type.ERROR, createLink(repository), "importLfsFailed", createParameters(lfsCount));
  }

  private static Map<String, String> createParameters(PullResponse.LfsCount lfsCount) {
    return Map.of(
      "successCount", Integer.toString(lfsCount.getSuccessCount()),
      "failureCount", Integer.toString(lfsCount.getFailureCount()),
      "overallCount", Integer.toString(lfsCount.getSuccessCount() + lfsCount.getFailureCount())
    );
  }

  private Notification getImportFailedNotification() {
    return new Notification(Type.ERROR, null, "importFailed");
  }

  private static String createLink(Repository repository) {
    return "/repo/" + repository.getNamespaceAndName();
  }
}
