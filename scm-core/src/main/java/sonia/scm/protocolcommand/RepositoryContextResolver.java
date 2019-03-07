package sonia.scm.protocolcommand;

@FunctionalInterface
public interface RepositoryContextResolver {

  RepositoryContext resolve(String[] args);

}
