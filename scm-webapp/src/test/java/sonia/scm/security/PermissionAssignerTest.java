package sonia.scm.security;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.store.InMemoryConfigurationEntryStoreFactory;
import sonia.scm.util.ClassLoaders;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SubjectAware(configuration = "classpath:sonia/scm/shiro-001.ini", username = "dent", password = "secret")
public class PermissionAssignerTest {

  @Rule
  public ShiroRule shiroRule = new ShiroRule();

  private DefaultSecuritySystem securitySystem;
  private PermissionAssigner permissionAssigner;

  @Before
  public void init() {
    PluginLoader pluginLoader = mock(PluginLoader.class);
    when(pluginLoader.getUberClassLoader()).thenReturn(ClassLoaders.getContextClassLoader(DefaultSecuritySystem.class));

    securitySystem = new DefaultSecuritySystem(new InMemoryConfigurationEntryStoreFactory(), pluginLoader);

    securitySystem.addPermission(new AssignedPermission("1", "perm:read:1"));
    securitySystem.addPermission(new AssignedPermission("1", "perm:read:2"));
    securitySystem.addPermission(new AssignedPermission("2", "perm:read:2"));
    securitySystem.addPermission(new AssignedPermission("1", true, "perm:read:2"));
    permissionAssigner = new PermissionAssigner(securitySystem);
  }

  @Test
  public void shouldFindUserPermissions() {
    Collection<PermissionDescriptor> permissionDescriptors = permissionAssigner.readPermissionsForUser("1");

    Assertions.assertThat(permissionDescriptors).hasSize(2);
  }

  @Test
  public void shouldOverwriteUserPermissions() {
    permissionAssigner.setPermissionsForUser("2", asList(new PermissionDescriptor("perm:read:3"), new PermissionDescriptor("perm:read:4")));

    Collection<PermissionDescriptor> permissionDescriptors = permissionAssigner.readPermissionsForUser("2");

    Assertions.assertThat(permissionDescriptors).hasSize(2);
  }
}
