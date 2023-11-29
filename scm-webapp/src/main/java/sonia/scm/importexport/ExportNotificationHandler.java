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

import jakarta.inject.Inject;
import sonia.scm.notifications.Notification;
import sonia.scm.notifications.NotificationSender;
import sonia.scm.notifications.Type;
import sonia.scm.repository.Repository;

public class ExportNotificationHandler {

  private final NotificationSender notificationSender;

  @Inject
  public ExportNotificationHandler(NotificationSender notificationSender) {
    this.notificationSender = notificationSender;
  }

  public void handleFailedExport(Repository repository) {
    notificationSender.send(getExportFailedNotification(repository));
  }

  public void handleSuccessfulExport(Repository repository) {
    notificationSender.send(getExportSuccessfulNotification(repository));
  }

  private Notification getExportFailedNotification(Repository repository) {
    return new Notification(Type.ERROR, "/repo/" + repository.getNamespaceAndName() + "/settings/general", "exportFailed");
  }

  private Notification getExportSuccessfulNotification(Repository repository) {
    return new Notification(Type.SUCCESS, "/repo/" + repository.getNamespaceAndName() + "/settings/general", "exportFinished");
  }

}
