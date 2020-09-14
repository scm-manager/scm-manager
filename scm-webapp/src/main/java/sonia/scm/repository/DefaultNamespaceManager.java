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

import javax.inject.Inject;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

public class DefaultNamespaceManager implements NamespaceManager {

  private final RepositoryManager repositoryManager;
  private final NamespaceDao dao;

  @Inject
  public DefaultNamespaceManager(RepositoryManager repositoryManager, NamespaceDao dao) {
    this.repositoryManager = repositoryManager;
    this.dao = dao;
  }

  @Override
  public Optional<Namespace> get(String namespace) {
    return repositoryManager
      .getAllNamespaces()
      .stream()
      .filter(n -> n.equals(namespace))
      .map(this::createNamespaceForName)
      .findFirst();
  }

  @Override
  public Collection<Namespace> getAll() {
    return repositoryManager
      .getAllNamespaces()
      .stream()
      .map(this::createNamespaceForName)
      .collect(Collectors.toList());
  }

  @Override
  public void modify(Namespace namespace) {
    if (!get(namespace.getNamespace()).isPresent()) {
      throw notFound(entity("Namespace", namespace.getNamespace()));
    }
    dao.add(namespace);
  }

  private Namespace createNamespaceForName(String namespace) {
    return dao.get(namespace)
      .map(Namespace::clone)
      .orElse(new Namespace(namespace));
  }
}
