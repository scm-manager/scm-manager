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

package sonia.scm.repository;

import com.github.legman.ReferenceType;
import com.github.legman.Subscribe;
import sonia.scm.HandlerEventType;
import sonia.scm.plugin.Extension;
import sonia.scm.store.StoreDeletionNotifier;

@Extension
class RepositoryDeletionNotifier implements StoreDeletionNotifier {
  private DeletionHandler handler;
  @Override
  public void registerHandler(DeletionHandler handler) {
    this.handler = handler;
  }

  @Subscribe(referenceType = ReferenceType.STRONG)
  public void onDelete(RepositoryEvent event) {
    if (handler != null && event.getEventType() == HandlerEventType.DELETE) {
      handler.notifyDeleted(Repository.class, event.getItem().getId());
    }
  }
}
