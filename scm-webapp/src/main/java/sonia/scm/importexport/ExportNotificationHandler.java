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
