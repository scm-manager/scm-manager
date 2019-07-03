package sonia.scm.legacy;

import com.google.inject.Inject;
import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricher;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.Index;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.plugin.Extension;

import javax.inject.Provider;

@Extension
@Enrich(Index.class)
public class LegacyIndexHalEnricher implements HalEnricher {

  private Provider<ScmPathInfoStore> scmPathInfoStoreProvider;

  @Inject
  public LegacyIndexHalEnricher(Provider<ScmPathInfoStore> scmPathInfoStoreProvider) {
    this.scmPathInfoStoreProvider = scmPathInfoStoreProvider;
  }

  private String createLink() {
    return new LinkBuilder(scmPathInfoStoreProvider.get().get(), LegacyRepositoryService.class)
      .method("getNameAndNamespaceForRepositoryId")
      .parameters("REPOID")
      .href()
      .replace("REPOID", "{id}");
  }

  @Override
  public void enrich(HalEnricherContext context, HalAppender appender) {
    appender.appendLink("nameAndNamespace", createLink());
  }
}
