package sonia.scm.protocolcommand.git;

import com.google.common.base.Splitter;
import sonia.scm.protocolcommand.RepositoryContext;
import sonia.scm.protocolcommand.RepositoryContextResolver;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.repository.RepositoryManager;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.Iterator;

public class GitRepositoryContextResolver implements RepositoryContextResolver {

  private RepositoryManager repositoryManager;
  private RepositoryLocationResolver locationResolver;

  @Inject
  public GitRepositoryContextResolver(RepositoryManager repositoryManager, RepositoryLocationResolver locationResolver) {
    this.repositoryManager = repositoryManager;
    this.locationResolver = locationResolver;
  }

  public RepositoryContext resolve(String[] args) {
    NamespaceAndName namespaceAndName = extractNamespaceAndName(args);
    Repository repository = repositoryManager.get(namespaceAndName);
    Path path = locationResolver.getPath(repository.getId()).resolve("data");
    return new RepositoryContext(repository, path);
  }

  private NamespaceAndName extractNamespaceAndName(String[] args) {
    String path = args[args.length - 1];
    Iterator<String> it = Splitter.on('/').omitEmptyStrings().split(path).iterator();
    String type = it.next();
    if ("repo".equals(type)) {
      String ns = it.next();
      String name = it.next();
      return new NamespaceAndName(ns, name);
    }
    return null;
  }
}
