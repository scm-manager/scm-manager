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

import com.google.inject.Inject;
import sonia.scm.repository.PermissionProvider;
import sonia.scm.repository.RepositoryRole;
import sonia.scm.repository.RepositoryRoleDAO;

import java.util.AbstractList;
import java.util.Collection;
import java.util.List ;

public class RepositoryPermissionProvider implements PermissionProvider {

  private final SystemRepositoryPermissionProvider systemRepositoryPermissionProvider;
  private final RepositoryRoleDAO repositoryRoleDAO;

  @Inject
  public RepositoryPermissionProvider(SystemRepositoryPermissionProvider systemRepositoryPermissionProvider, RepositoryRoleDAO repositoryRoleDAO) {
    this.systemRepositoryPermissionProvider = systemRepositoryPermissionProvider;
    this.repositoryRoleDAO = repositoryRoleDAO;
  }

  public Collection<String> availableVerbs() {
    return systemRepositoryPermissionProvider.availableVerbs();
  }

  public Collection<String> readOnlyVerbs() {
    return systemRepositoryPermissionProvider.readOnlyVerbs();
  }

  public Collection<RepositoryRole> availableRoles() {
    List<RepositoryRole> availableSystemRoles = systemRepositoryPermissionProvider.availableRoles();
    List<RepositoryRole> customRoles = repositoryRoleDAO.getAll();

    return new AbstractList<RepositoryRole>() {
      @Override
      public RepositoryRole get(int index) {
        return index < availableSystemRoles.size()? availableSystemRoles.get(index): customRoles.get(index - availableSystemRoles.size());
      }

      @Override
      public int size() {
        return availableSystemRoles.size() + customRoles.size();
      }
    };
  }
}
