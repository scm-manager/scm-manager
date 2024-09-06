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

import org.junit.jupiter.api.Test;
import sonia.scm.security.AssignedPermission;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.InMemoryByteConfigurationEntryStoreFactory;

import static org.assertj.core.api.Assertions.assertThat;

class NamespacePermissionsUpdateStepTest {

  private final InMemoryByteConfigurationEntryStoreFactory entryStoreFactory = new InMemoryByteConfigurationEntryStoreFactory();
  private final NamespacePermissionsUpdateStep updateStep = new NamespacePermissionsUpdateStep(entryStoreFactory);

  @Test
  void shouldUpdatePermissions() throws Exception {
    ConfigurationEntryStore<AssignedPermission> securityStore = createSecurityStore();
    securityStore.put(new AssignedPermission("trillian", false, "namespace:permissionRead"));
    securityStore.put(new AssignedPermission("dent", true, "namespace:permissionRead,permissionWrite"));

    updateStep.doUpdate();

    assertThat(securityStore.getAll().values())
      .hasSize(2)
      .contains(new AssignedPermission("trillian", false, "namespace:permissionRead:*"))
      .contains(new AssignedPermission("dent", true, "namespace:permissionRead,permissionWrite:*"));
  }

  private ConfigurationEntryStore<AssignedPermission> createSecurityStore() {
    return entryStoreFactory.withType(AssignedPermission.class).withName("security").build();
  }
}
