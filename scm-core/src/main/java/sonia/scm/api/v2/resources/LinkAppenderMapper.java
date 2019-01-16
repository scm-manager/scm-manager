package sonia.scm.api.v2.resources;

import com.google.common.annotations.VisibleForTesting;

import javax.inject.Inject;

public class LinkAppenderMapper {

  @Inject
  private LinkEnricherRegistry registry;

  @VisibleForTesting
  void setRegistry(LinkEnricherRegistry registry) {
    this.registry = registry;
  }

  protected void appendLinks(LinkAppender appender, Object source, Object... contextEntries) {
    // null check is only their to not break existing tests
    if (registry != null) {

      Object[] ctx = new Object[contextEntries.length + 1];
      ctx[0] = source;
      for (int i = 0; i < contextEntries.length; i++) {
        ctx[i + 1] = contextEntries[i];
      }

      LinkEnricherContext context = LinkEnricherContext.of(ctx);

      Iterable<LinkEnricher> enrichers = registry.allByType(source.getClass());
      for (LinkEnricher enricher : enrichers) {
        enricher.enrich(context, appender);
      }
    }
  }

}
