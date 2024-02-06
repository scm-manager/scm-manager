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


import sonia.scm.ManagerDecorator;
import sonia.scm.Type;

import java.io.IOException;
import java.util.Collection;

/**
 * Decorator for {@link RepositoryManager}.
 *
 * @since 1.23
 */
public class RepositoryManagerDecorator
  extends ManagerDecorator<Repository>
  implements RepositoryManager {

  private final RepositoryManager decorated;

  public RepositoryManagerDecorator(RepositoryManager decorated) {
    super(decorated);
    this.decorated = decorated;
  }


 
  @Override
  public void fireHookEvent(RepositoryHookEvent event) {
    decorated.fireHookEvent(event);
  }

 
  @Override
  public void importRepository(Repository repository) throws IOException {
    decorated.importRepository(repository);
  }


  @Override
  public Repository get(NamespaceAndName namespaceAndName) {
    return decorated.get(namespaceAndName);
  }

  @Override
  public Collection<RepositoryType> getConfiguredTypes() {
    return decorated.getConfiguredTypes();
  }

  /**
   * Returns the decorated {@link RepositoryManager}.
   *
   * @return decorated {@link RepositoryManager}
   * @since 1.34
   */
  public RepositoryManager getDecorated() {
    return decorated;
  }

  @Override
  @SuppressWarnings("unchecked")
  public RepositoryHandler getHandler(String type) {
    return decorated.getHandler(type);
  }

  @Override
  public Collection<Type> getTypes() {
    return decorated.getTypes();
  }

  @Override
  public Repository rename(Repository repository, String newNamespace, String newName) {
    return decorated.rename(repository, newNamespace, newName);
  }

  @Override
  public Collection<String> getAllNamespaces() {
    return decorated.getAllNamespaces();
  }

  @Override
  public void archive(Repository repository) {
    decorated.archive(repository);
  }

  @Override
  public void unarchive(Repository repository) {
    decorated.unarchive(repository);
  }

}
