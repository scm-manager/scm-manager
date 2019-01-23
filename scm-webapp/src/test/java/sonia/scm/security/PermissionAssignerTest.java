package sonia.scm.security;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import org.apache.shiro.authz.UnauthorizedException;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import sonia.scm.NotFoundException;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.store.InMemoryConfigurationEntryStoreFactory;
import sonia.scm.util.ClassLoaders;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SubjectAware(configuration = "classpath:sonia/scm/shiro-001.ini", username = "dent", password = "secret")
public class PermissionAssignerTest {

  @Rule
  public ShiroRule shiroRule = new ShiroRule();

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private DefaultSecuritySystem securitySystem;
  private PermissionAssigner permissionAssigner;

  @Before
  public void init() {
    PluginLoader pluginLoader = mock(PluginLoader.class);
    when(pluginLoader.getUberClassLoader()).thenReturn(ClassLoaders.getContextClassLoader(DefaultSecuritySystem.class));

    securitySystem = new DefaultSecuritySystem(new InMemoryConfigurationEntryStoreFactory(), pluginLoader) {
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

    Assertions.assertThat(permissionDescriptors).hasSize(2);
  }

  @Test
  public void shouldFindGroupPermissions() {
    Collection<PermissionDescriptor> permissionDescriptors = permissionAssigner.readPermissionsForUser("1");

    Assertions.assertThat(permissionDescriptors).hasSize(2);
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

    Assertions.assertThat(permissionDescriptors).hasSize(2);
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
  }
}
