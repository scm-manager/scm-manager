package sonia.scm.protocolcommand.git;

import com.google.inject.Inject;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ReceivePack;
import org.eclipse.jgit.transport.resolver.ReceivePackFactory;
import sonia.scm.protocolcommand.RepositoryContext;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.spi.HookEventFacade;
import sonia.scm.web.CollectingPackParserListener;
import sonia.scm.web.GitReceiveHook;

/**
 * TODO we should have a single/abstract ReceivePackFactory for http and ssh.
 */
public class SshReceivePackFactory implements ReceivePackFactory<RepositoryContext> {

  private final GitRepositoryHandler handler;
  private final GitReceiveHook hook;

  @Inject
  public SshReceivePackFactory(GitRepositoryHandler handler, HookEventFacade hookEventFacade) {
    this.handler = handler;
    this.hook = new GitReceiveHook(hookEventFacade, handler);
  }

  @Override
  public ReceivePack create(RepositoryContext repositoryContext, Repository repository) {
    ReceivePack receivePack = new ReceivePack(repository);
    receivePack.setAllowNonFastForwards(isNonFastForwardAllowed());

    receivePack.setPreReceiveHook(hook);
    receivePack.setPostReceiveHook(hook);

    // apply collecting listener, to be able to check which commits are new
    CollectingPackParserListener.set(receivePack);

    return receivePack;
  }

  private boolean isNonFastForwardAllowed() {
    return ! handler.getConfig().isNonFastForwardDisallowed();
  }
}
