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
