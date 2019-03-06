package sonia.scm.protocolcommand.git;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.UploadPack;
import org.eclipse.jgit.transport.resolver.UploadPackFactory;
import sonia.scm.protocolcommand.RepositoryContext;

public class ScmUploadPackFactory implements UploadPackFactory<RepositoryContext> {
  @Override
  public UploadPack create(RepositoryContext repositoryContext, Repository repository) {
    return new UploadPack(repository);
  }
}
