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
