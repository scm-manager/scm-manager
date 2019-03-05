package sonia.scm.protocolcommand.git;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ReceivePack;
import org.eclipse.jgit.transport.resolver.ReceivePackFactory;
import org.eclipse.jgit.transport.resolver.ServiceNotAuthorizedException;
import org.eclipse.jgit.transport.resolver.ServiceNotEnabledException;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.spi.HookEventFacade;
import sonia.scm.web.CollectingPackParserListener;
import sonia.scm.web.GitReceiveHook;

public abstract class BaseReceivePackFactory<T> implements ReceivePackFactory<T> {

  private final GitRepositoryHandler handler;
  private final GitReceiveHook hook;

  protected BaseReceivePackFactory(GitRepositoryHandler handler, HookEventFacade hookEventFacade) {
    this.handler = handler;
    this.hook = new GitReceiveHook(hookEventFacade, handler);
  }

  @Override
  public final ReceivePack create(T connection, Repository repository) throws ServiceNotAuthorizedException, ServiceNotEnabledException {
    ReceivePack receivePack = createBasicReceivePack(connection, repository);
    receivePack.setAllowNonFastForwards(isNonFastForwardAllowed());

    receivePack.setPreReceiveHook(hook);
    receivePack.setPostReceiveHook(hook);
    // apply collecting listener, to be able to check which commits are new
    CollectingPackParserListener.set(receivePack);

    return receivePack;
  }

  protected abstract ReceivePack createBasicReceivePack(T request, Repository repository)
    throws ServiceNotEnabledException, ServiceNotAuthorizedException;

  private boolean isNonFastForwardAllowed() {
    return ! handler.getConfig().isNonFastForwardDisallowed();
  }
}
