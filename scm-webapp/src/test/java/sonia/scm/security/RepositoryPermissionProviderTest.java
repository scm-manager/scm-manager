package sonia.scm.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.util.ClassLoaders;

import java.lang.reflect.Field;
import java.util.Arrays;

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
    ConfigurationEntryStoreFactory configurationEntryStoreFactory = mock(ConfigurationEntryStoreFactory.class);
    repositoryPermissionProvider = new RepositoryPermissionProvider(configurationEntryStoreFactory, pluginLoader);
    allVerbsFromRepositoryClass = Arrays.stream(RepositoryPermissions.class.getDeclaredFields())
      .filter(field -> field.getName().startsWith("ACTION_"))
      .map(this::getString)
      .filter(verb -> !"create".equals(verb))
      .toArray(String[]::new);
  }

  @Test
  void shouldReadAvailableRoles() {
    assertThat(repositoryPermissionProvider.availableRoles()).isNotEmpty();
    assertThat(repositoryPermissionProvider.availableRoles()).allSatisfy(this::eitherStarOrOnlyAvailableVerbs);
  }

  private void eitherStarOrOnlyAvailableVerbs(RepositoryPermissionProvider.RoleDescriptor role) {
    if (!role.getVerbs().contains("*") || role.getVerbs().size() > 1) {
      assertThat(role.getVerbs()).isSubsetOf(allVerbsFromRepositoryClass);
    }
  }

  @Test
  void shouldReadAvailableVerbsFromRepository() {
    assertThat(repositoryPermissionProvider.availableVerbs()).contains(allVerbsFromRepositoryClass);
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
