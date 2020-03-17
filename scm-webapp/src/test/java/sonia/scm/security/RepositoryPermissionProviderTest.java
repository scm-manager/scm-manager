/**
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.RepositoryRole;
import sonia.scm.repository.RepositoryRoleDAO;

import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryPermissionProviderTest {

  @Mock
  SystemRepositoryPermissionProvider systemRepositoryPermissionProvider;
  @Mock
  RepositoryRoleDAO repositoryRoleDAO;

  @InjectMocks
  RepositoryPermissionProvider repositoryPermissionProvider;

  @Test
  void shouldReturnVerbsFromSystem() {
    List<String> expectedVerbs = asList("verb1", "verb2");
    when(systemRepositoryPermissionProvider.availableVerbs()).thenReturn(expectedVerbs);

    Collection<String> actualVerbs = repositoryPermissionProvider.availableVerbs();

    assertThat(actualVerbs).isEqualTo(expectedVerbs);
  }

  @Test
  void shouldReturnJoinedRolesFromSystemAndDao() {
    RepositoryRole systemRole = new RepositoryRole("roleSystem", singletonList("verb1"), "system");
    RepositoryRole daoRole = new RepositoryRole("roleDao", singletonList("verb1"), "xml");
    when(systemRepositoryPermissionProvider.availableRoles()).thenReturn(singletonList(systemRole));
    when(repositoryRoleDAO.getAll()).thenReturn(singletonList(daoRole));

    Collection<RepositoryRole> actualRoles = repositoryPermissionProvider.availableRoles();

    assertThat(actualRoles).containsExactly(systemRole, daoRole);
  }
}
