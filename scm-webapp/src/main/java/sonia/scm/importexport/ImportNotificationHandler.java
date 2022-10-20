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

import javax.inject.Inject;

public class ImportNotificationHandler {

  private final NotificationSender notificationSender;

  @Inject
  public ImportNotificationHandler(NotificationSender notificationSender) {
    this.notificationSender = notificationSender;
  }

  public void handleSuccessfulImport(Repository repository) {
    notificationSender.send(getImportSuccessfulNotification(repository));
  }

  public void handleSuccessfulImportWithLfsFailures(Repository repository) {
    notificationSender.send(getImportLfsFailedNotification(repository));
  }

  public void handleFailedImport(Repository repository) {
    notificationSender.send(getImportFailedNotification(repository));
  }

  private Notification getImportSuccessfulNotification(Repository repository) {
    return new Notification(Type.SUCCESS, createLink(repository), "importFinished");
  }

  private Notification getImportLfsFailedNotification(Repository repository) {
    return new Notification(Type.ERROR, createLink(repository), "importLfsFailed");
  }

  private Notification getImportFailedNotification(Repository repository) {
    return new Notification(Type.ERROR, null, "importFailed");
  }

  private static String createLink(Repository repository) {
    return "/repo/" + repository.getNamespaceAndName();
  }
}
