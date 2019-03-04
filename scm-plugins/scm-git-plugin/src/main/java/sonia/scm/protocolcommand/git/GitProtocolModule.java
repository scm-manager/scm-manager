package sonia.scm.protocolcommand.git;

import com.google.inject.servlet.ServletModule;
import sonia.scm.plugin.Extension;
import sonia.scm.protocolcommand.RepositoryContextResolver;
import sonia.scm.protocolcommand.ScmSshProtocol;

@Extension
public class GitProtocolModule extends ServletModule {
  @Override
  protected void configureServlets() {
    bind(RepositoryContextResolver.class).to(GitRepositoryContextResolver.class);
    bind(ScmSshProtocol.class).to(GitSshProtocol.class);
  }
}
