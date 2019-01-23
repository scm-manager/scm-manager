package sonia.scm.security;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.util.ClassLoaders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RepositoryPermissionsTest {

  private RepositoryPermissions repositoryPermissions;

  @BeforeEach
  void init() {
    PluginLoader pluginLoader = mock(PluginLoader.class);
    when(pluginLoader.getUberClassLoader()).thenReturn(ClassLoaders.getContextClassLoader(DefaultSecuritySystem.class));
    ConfigurationEntryStoreFactory configurationEntryStoreFactory = mock(ConfigurationEntryStoreFactory.class);
    repositoryPermissions = new RepositoryPermissions(configurationEntryStoreFactory, pluginLoader);
  }

  @Test
  void shouldReadAvailableRoles() {
    Assertions.assertThat(repositoryPermissions.availableRoles()).isNotEmpty().noneMatch(r -> r.getVerbs().isEmpty());
    System.out.println(repositoryPermissions.availableRoles());
  }

  @Test
  void shouldReadAvailableVerbs() {
    Assertions.assertThat(repositoryPermissions.availableVerbs()).isNotEmpty();
    System.out.println(repositoryPermissions.availableVerbs());
  }
}
