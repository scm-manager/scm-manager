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
