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

package sonia.scm.store;

import jakarta.inject.Inject;
import sonia.scm.EagerSingleton;
import sonia.scm.plugin.Extension;

import java.util.Collection;
import java.util.Set;

@Extension
@EagerSingleton
class QueryableStoreDeletionHandler implements StoreDeletionNotifier.DeletionHandler {

  private final StoreMetaDataProvider metaDataProvider;
  private final QueryableStoreFactory storeFactory;

  @Inject
  QueryableStoreDeletionHandler(Set<StoreDeletionNotifier> notifiers, StoreMetaDataProvider metaDataProvider, QueryableStoreFactory storeFactory) {
    this.metaDataProvider = metaDataProvider;
    this.storeFactory = storeFactory;
    notifiers.forEach(notifier -> notifier.registerHandler(this));
  }

  @Override
  public void notifyDeleted(StoreDeletionNotifier.ClassWithId... classWithIds) {
    Class<?>[] classes = new Class[classWithIds.length];
    String[] ids = new String[classWithIds.length];
    for (int i = 0; i < classWithIds.length; i++) {
      classes[i] = classWithIds[i].clazz();
      ids[i] = classWithIds[i].id();
    }
    Collection<Class<?>> typesWithParent = metaDataProvider.getTypesWithParent(classes);
    typesWithParent.forEach(type -> storeFactory.getForMaintenance(type, ids).clear());
  }
}
