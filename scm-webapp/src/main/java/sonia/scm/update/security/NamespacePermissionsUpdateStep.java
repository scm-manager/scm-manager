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

package sonia.scm.update.security;

import jakarta.inject.Inject;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.security.AssignedPermission;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.version.Version;

import java.util.HashSet;

@Extension
public class NamespacePermissionsUpdateStep implements UpdateStep {

  private final ConfigurationEntryStoreFactory configurationEntryStoreFactory;

  @Inject
  public NamespacePermissionsUpdateStep(ConfigurationEntryStoreFactory configurationEntryStoreFactory) {
    this.configurationEntryStoreFactory = configurationEntryStoreFactory;
  }

  @Override
  public void doUpdate() throws Exception {
    ConfigurationEntryStore<AssignedPermission> securityStore = createSecurityStore();
    HashSet<String> toBeRemoved = new HashSet<>();
    HashSet<AssignedPermission> toBeAdded = new HashSet<>();
    securityStore.getAll().forEach((k, v) -> {
      if (v.getPermission().getValue().equals("namespace:permissionRead")) {
        toBeAdded.add(new AssignedPermission(v.getName(), v.isGroupPermission(), "namespace:permissionRead:*"));
        toBeRemoved.add(k);
      }
      if (v.getPermission().getValue().equals("namespace:permissionRead,permissionWrite")) {
        toBeAdded.add(new AssignedPermission(v.getName(), v.isGroupPermission(), "namespace:permissionRead,permissionWrite:*"));
        toBeRemoved.add(k);
      }
    });
    toBeAdded.forEach(securityStore::put);
    toBeRemoved.forEach(securityStore::remove);
  }

  private ConfigurationEntryStore<AssignedPermission> createSecurityStore() {
    return configurationEntryStoreFactory.withType(AssignedPermission.class).withName("security").build();
  }

  @Override
  public Version getTargetVersion() {
    return Version.parse("3.1.0");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.security.xml";
  }
}
