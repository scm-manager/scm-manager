package sonia.scm.security;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.util.ClassLoaders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RepositoryPermissionAssignerTest {

  @Test
  void x() {
    PluginLoader pluginLoader = mock(PluginLoader.class);
    when(pluginLoader.getUberClassLoader()).thenReturn(ClassLoaders.getContextClassLoader(DefaultSecuritySystem.class));
    ConfigurationEntryStoreFactory configurationEntryStoreFactory = mock(ConfigurationEntryStoreFactory.class);
    RepositoryPermissionAssigner repositoryPermissionAssigner = new RepositoryPermissionAssigner(configurationEntryStoreFactory, pluginLoader);
    Assertions.assertThat(repositoryPermissionAssigner.availableVerbs()).isNotEmpty();
    Assertions.assertThat(repositoryPermissionAssigner.availableRoles()).isNotEmpty().noneMatch(r -> r.getVerbs().isEmpty());
    System.out.println(repositoryPermissionAssigner.availableVerbs());
    System.out.println(repositoryPermissionAssigner.availableRoles());
  }
}
