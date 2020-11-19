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

package sonia.scm.repository;

import com.github.legman.Subscribe;
import sonia.scm.EagerSingleton;
import sonia.scm.Initable;
import sonia.scm.SCMContextProvider;
import sonia.scm.plugin.Extension;

import javax.inject.Inject;

@Extension
@EagerSingleton
public class RepositoryPermissionsInitializer implements Initable {

  private final RepositoryDAO repositoryDAO;
  private final PermissionProvider permissionProvider;

  @Inject
  public RepositoryPermissionsInitializer(RepositoryDAO repositoryDAO, PermissionProvider permissionProvider) {
    this.repositoryDAO = repositoryDAO;
    this.permissionProvider = permissionProvider;
  }

  @Override
  public void init(SCMContextProvider context) {
    repositoryDAO.getAll()
      .stream()
      .filter(Repository::isArchived)
      .map(Repository::getId)
      .forEach(RepositoryPermissions::setAsArchived);

    RepositoryPermissions.setReadOnlyVerbs(permissionProvider.readOnlyVerbs());
  }

  @Subscribe(async = false)
  public void updateListener(RepositoryModificationEvent event) {
    Repository repository = event.getItem();
    if (repository.isArchived()) {
      RepositoryPermissions.setAsArchived(repository.getId());
    } else {
      RepositoryPermissions.removeFromArchived(repository.getId());
    }
  }
}
