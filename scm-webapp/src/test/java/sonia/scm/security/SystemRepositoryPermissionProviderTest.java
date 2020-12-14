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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.RepositoryRole;
import sonia.scm.util.ClassLoaders;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SystemRepositoryPermissionProviderTest {

  private SystemRepositoryPermissionProvider repositoryPermissionProvider;
  private String[] allVerbsFromRepositoryClass;


  @BeforeEach
  void init() {
    PluginLoader pluginLoader = mock(PluginLoader.class);
    when(pluginLoader.getUberClassLoader()).thenReturn(ClassLoaders.getContextClassLoader(DefaultSecuritySystem.class));
    repositoryPermissionProvider = new SystemRepositoryPermissionProvider(pluginLoader);
    allVerbsFromRepositoryClass = Arrays.stream(RepositoryPermissions.class.getDeclaredFields())
      .filter(field -> field.getName().startsWith("ACTION_"))
      .filter(field -> !field.getName().equals("ACTION_HEALTHCHECK"))
      .map(this::getString)
      .filter(verb -> !asList("create", "archive").contains(verb))
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
