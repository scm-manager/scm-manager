/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
