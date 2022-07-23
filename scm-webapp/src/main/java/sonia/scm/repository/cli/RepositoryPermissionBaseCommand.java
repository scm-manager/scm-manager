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

package sonia.scm.repository.cli;

import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryRoleManager;

import javax.inject.Inject;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

class RepositoryPermissionBaseCommand extends PermissionBaseCommand<Repository> {

  private final RepositoryManager repositoryManager;

  @Inject
  RepositoryPermissionBaseCommand(RepositoryManager repositoryManager, RepositoryRoleManager roleManager, RepositoryTemplateRenderer templateRenderer) {
    super(roleManager, templateRenderer);
    this.repositoryManager = repositoryManager;
  }

  @Override
  Optional<Repository> get(String repositoryName) {
    NamespaceAndName namespaceAndName;
    try {
      namespaceAndName = NamespaceAndName.fromString(repositoryName);
    } catch (IllegalArgumentException e) {
      getTemplateRenderer().renderInvalidInputError();
      return empty();
    }
    Repository repository = repositoryManager.get(namespaceAndName);
    if (repository == null) {
      getTemplateRenderer().renderNotFoundError();
      return empty();
    } else {
      return of(repository);
    }
  }

  @Override
  void set(Repository repository) {
    repositoryManager.modify(repository);
  }
}
