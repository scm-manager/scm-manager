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

import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

class RepositoryPermissionBaseAdapter implements PermissionBaseAdapter<Repository> {

  private final RepositoryManager repositoryManager;
  private final RepositoryTemplateRenderer templateRenderer;

  RepositoryPermissionBaseAdapter(RepositoryManager repositoryManager, RepositoryTemplateRenderer templateRenderer) {
    this.repositoryManager = repositoryManager;
    this.templateRenderer = templateRenderer;
  }

  @Override
  public Optional<Repository> get(String repositoryNamespaceAndName) {
    NamespaceAndName namespaceAndName;
    try {
      namespaceAndName = NamespaceAndName.fromString(repositoryNamespaceAndName);
    } catch (IllegalArgumentException e) {
      templateRenderer.renderInvalidInputError();
      return empty();
    }
    Repository repository = repositoryManager.get(namespaceAndName);
    if (repository == null) {
      templateRenderer.renderNotFoundError();
      return empty();
    } else {
      return of(repository);
    }
  }

  @Override
  public void set(Repository repository) {
    repositoryManager.modify(repository);
  }
}
