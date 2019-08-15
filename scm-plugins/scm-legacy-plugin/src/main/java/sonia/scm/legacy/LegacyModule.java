package sonia.scm.legacy;

import com.google.inject.servlet.ServletModule;
import sonia.scm.plugin.Extension;

@Extension
public class LegacyModule extends ServletModule {

  @Override
  protected void configureServlets() {
    filter("/*").through(RepositoryLegacyProtocolRedirectFilter.class);
  }
}
