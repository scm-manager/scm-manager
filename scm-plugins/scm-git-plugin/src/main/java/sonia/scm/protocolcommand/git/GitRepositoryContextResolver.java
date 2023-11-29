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
