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

package sonia.scm.security;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import org.apache.shiro.authz.UnauthorizedException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import sonia.scm.NotFoundException;
import sonia.scm.auditlog.Auditor;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.store.InMemoryByteConfigurationEntryStoreFactory;
import sonia.scm.util.ClassLoaders;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SubjectAware(configuration = "classpath:sonia/scm/shiro-001.ini", username = "dent", password = "secret")
public class PermissionAssignerTest {

  @Rule
  public ShiroRule shiroRule = new ShiroRule();

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private DefaultSecuritySystem securitySystem;
  private PermissionAssigner permissionAssigner;

  private Auditor auditor = mock(Auditor.class);

  @Before
  public void init() {
    PluginLoader pluginLoader = mock(PluginLoader.class);
    when(pluginLoader.getUberClassLoader()).thenReturn(ClassLoaders.getContextClassLoader(DefaultSecuritySystem.class));

    securitySystem = new DefaultSecuritySystem(new InMemoryByteConfigurationEntryStoreFactory(), pluginLoader, Set.of(auditor)) {
      @Override
      public Collection<PermissionDescriptor> getAvailablePermissions() {
        return Arrays.stream(new String[]{"perm:read:1", "perm:read:2", "perm:read:3", "perm:read:4"})
          .map(PermissionDescriptor::new)
          .collect(Collectors.toList());
      }
    };

    try {
      securitySystem.addPermission(new AssignedPermission("1", "perm:read:1"));
      securitySystem.addPermission(new AssignedPermission("1", "perm:read:2"));
      securitySystem.addPermission(new AssignedPermission("2", "perm:read:2"));
      securitySystem.addPermission(new AssignedPermission("1", true, "perm:read:2"));
    } catch (UnauthorizedException e) {
      // ignore for tests with limited privileges
    }
    permissionAssigner = new PermissionAssigner(securitySystem);
  }

  @Test
  public void shouldFindUserPermissions() {
    Collection<PermissionDescriptor> permissionDescriptors = permissionAssigner.readPermissionsForUser("1");

    assertThat(permissionDescriptors).hasSize(2);
  }

  @Test
  public void shouldFindGroupPermissions() {
    Collection<PermissionDescriptor> permissionDescriptors = permissionAssigner.readPermissionsForUser("1");

    assertThat(permissionDescriptors).hasSize(2);
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldNotReadUserPermissionsForUnprivilegedUser() {
    expectedException.expect(UnauthorizedException.class);

    permissionAssigner.readPermissionsForUser("1");
  }

  @Test
  public void shouldOverwriteUserPermissions() {
    permissionAssigner.setPermissionsForUser("2", asList(new PermissionDescriptor("perm:read:3"), new PermissionDescriptor("perm:read:4")));

    Collection<PermissionDescriptor> permissionDescriptors = permissionAssigner.readPermissionsForUser("2");

    assertThat(permissionDescriptors).hasSize(2);
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldNotOverwriteUserPermissionsForUnprivilegedUser() {
    expectedException.expect(UnauthorizedException.class);

    permissionAssigner.setPermissionsForUser("2", asList(new PermissionDescriptor("perm:read:3"), new PermissionDescriptor("perm:read:4")));
  }

  @Test
  public void shouldFailForNotExistingPermissions() {
    expectedException.expect(NotFoundException.class);
    permissionAssigner.setPermissionsForUser("2", asList(new PermissionDescriptor("perm:read:4"), new PermissionDescriptor("perm:read:5")));
  }

  @Test
  public void shouldAcceptNotExistingPermissionsWhenTheyWereAssignedBefore() {
    securitySystem.addPermission(new AssignedPermission("2", "perm:read:5"));

    permissionAssigner.setPermissionsForUser("2", asList(new PermissionDescriptor("perm:read:5")));

    assertThat(permissionAssigner.readPermissionsForUser("2")).hasSize(1);
  }

  @Test
  public void shouldCallAuditorForCreation() {
    reset(auditor);

    permissionAssigner.setPermissionsForUser("2", asList(new PermissionDescriptor("perm:read:2"), new PermissionDescriptor("perm:read:4")));

    verify(auditor).createEntry(argThat(
      context -> {
        assertThat(context.getEntity()).isEqualTo("2");
        assertThat(context.getAdditionalLabels()).contains("user");
        assertThat(context.getOldObject()).isNull();
        assertThat(context.getObject())
          .extracting("permission")
          .extracting("value")
          .isEqualTo("perm:read:4");
        return true;
      }
    ));
  }

  @Test
  public void shouldCallAuditorForRemoval() {
    reset(auditor);

    permissionAssigner.setPermissionsForUser("2", emptyList());

    verify(auditor).createEntry(argThat(
      context -> {
        assertThat(context.getEntity()).isEqualTo("2");
        assertThat(context.getAdditionalLabels()).contains("user");
        assertThat(context.getObject()).isNull();
        assertThat(context.getOldObject())
          .extracting("permission")
          .extracting("value")
          .isEqualTo("perm:read:2");
        return true;
      }
    ));
  }
}
