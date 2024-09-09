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

package sonia.scm.protocolcommand.git;

import com.google.common.base.Splitter;
import jakarta.inject.Inject;
import sonia.scm.ContextEntry;
import sonia.scm.NotFoundException;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.protocolcommand.RepositoryContext;
import sonia.scm.protocolcommand.RepositoryContextResolver;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.repository.RepositoryManager;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

public class GitRepositoryContextResolver implements RepositoryContextResolver {

  private final RepositoryManager repositoryManager;
  private final RepositoryLocationResolver locationResolver;
  private final ScmConfiguration configuration;

  @Inject
  public GitRepositoryContextResolver(RepositoryManager repositoryManager, RepositoryLocationResolver locationResolver, ScmConfiguration configuration) {
    this.repositoryManager = repositoryManager;
    this.locationResolver = locationResolver;
    this.configuration = configuration;
  }

  public RepositoryContext resolve(String[] args) {
    NamespaceAndName namespaceAndName = extractNamespaceAndName(args).orElseThrow(() -> NotFoundException.notFound(ContextEntry.ContextBuilder.entity("path", String.join("/", args))));
    Repository repository = repositoryManager.get(namespaceAndName);
    Path path = locationResolver.forClass(Path.class).getLocation(repository.getId()).resolve("data");
    return new RepositoryContext(repository, path);
  }

  private Optional<NamespaceAndName> extractNamespaceAndName(String[] args) {
    String path = args[args.length - 1];
    Iterator<String> it = Splitter.on('/').omitEmptyStrings().split(path).iterator();
    String type = it.next();
    if (type.equals(configuration.getServerContextPath())) {
      type = it.next();
    }
    if ("repo".equals(type)) {
      String ns = it.next();
      String name = it.next();
      return of((new NamespaceAndName(ns, name)));
    }
    return empty();
  }
}
