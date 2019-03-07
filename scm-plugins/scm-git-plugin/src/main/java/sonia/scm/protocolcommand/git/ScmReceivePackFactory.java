package sonia.scm.protocolcommand.git;

import com.google.inject.Inject;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ReceivePack;
import sonia.scm.protocolcommand.RepositoryContext;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.spi.HookEventFacade;

public class ScmReceivePackFactory extends BaseReceivePackFactory<RepositoryContext> {

  @Inject
  public ScmReceivePackFactory(GitRepositoryHandler handler, HookEventFacade hookEventFacade) {
    super(handler, hookEventFacade);
  }

  @Override
  protected ReceivePack createBasicReceivePack(RepositoryContext repositoryContext, Repository repository) {
    return new ReceivePack(repository);
  }
}
