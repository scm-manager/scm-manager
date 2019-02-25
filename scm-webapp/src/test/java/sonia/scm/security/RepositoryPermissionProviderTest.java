package sonia.scm.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.util.ClassLoaders;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RepositoryPermissionProviderTest {

  private RepositoryPermissionProvider repositoryPermissionProvider;
  private String[] allVerbsFromRepositoryClass;


  @BeforeEach
  void init() {
    PluginLoader pluginLoader = mock(PluginLoader.class);
    when(pluginLoader.getUberClassLoader()).thenReturn(ClassLoaders.getContextClassLoader(DefaultSecuritySystem.class));
    repositoryPermissionProvider = new RepositoryPermissionProvider(pluginLoader);
    allVerbsFromRepositoryClass = Arrays.stream(RepositoryPermissions.class.getDeclaredFields())
      .filter(field -> field.getName().startsWith("ACTION_"))
      .filter(field -> !field.getName().equals("ACTION_HEALTHCHECK"))
      .map(this::getString)
      .filter(verb -> !"create".equals(verb))
      .toArray(String[]::new);
  }

  @Test
  void shouldReadAvailableRoles() {
    assertThat(repositoryPermissionProvider.availableRoles()).isNotEmpty();
    assertThat(repositoryPermissionProvider.availableRoles()).allSatisfy(this::containsOnlyAvailableVerbs);
  }

  private void containsOnlyAvailableVerbs(RepositoryRole role) {
    assertThat(role.getVerbs()).isSubsetOf(repositoryPermissionProvider.availableVerbs());
  }

  @Test
  void shouldReadAvailableVerbsFromRepository() {
    assertThat(repositoryPermissionProvider.availableVerbs()).contains(allVerbsFromRepositoryClass);
  }

  @Test
  void shouldMergeRepositoryRoles() {
    Collection<String> verbsInMergedRole = repositoryPermissionProvider
      .availableRoles()
      .stream()
      .filter(r -> "READ".equals(r.getName()))
      .findFirst()
      .get()
      .getVerbs();
    assertThat(verbsInMergedRole).contains("read", "pull", "test");
  }

  private String getString(Field field) {
    try {
      return (String) field.get(null);
    } catch (IllegalAccessException e) {
      fail(e);
      return null;
    }
  }
}
