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

import jakarta.inject.Inject;
import sonia.scm.notifications.Notification;
import sonia.scm.notifications.NotificationSender;
import sonia.scm.notifications.Type;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.PullResponse;

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
