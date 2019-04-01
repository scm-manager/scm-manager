package sonia.scm.repository.spi;

import org.eclipse.jgit.transport.ScmTransportProtocol;
import org.eclipse.jgit.transport.Transport;
import org.junit.rules.ExternalResource;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.PreProcessorUtil;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.api.HookContextFactory;

import static com.google.inject.util.Providers.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BindTransportProtocolRule extends ExternalResource {

  private ScmTransportProtocol scmTransportProtocol;

  @Override
  protected void before() throws Throwable {
    HookContextFactory hookContextFactory = new HookContextFactory(mock(PreProcessorUtil.class));
    RepositoryManager repositoryManager = mock(RepositoryManager.class);
    HookEventFacade hookEventFacade = new HookEventFacade(of(repositoryManager), hookContextFactory);
    GitRepositoryHandler gitRepositoryHandler = mock(GitRepositoryHandler.class);
    scmTransportProtocol = new ScmTransportProtocol(of(hookEventFacade), of(gitRepositoryHandler));

    Transport.register(scmTransportProtocol);

    when(gitRepositoryHandler.getRepositoryId(any())).thenReturn("1");
    when(repositoryManager.get("1")).thenReturn(new sonia.scm.repository.Repository());
  }

  @Override
  protected void after() {
    Transport.unregister(scmTransportProtocol);
  }
}
