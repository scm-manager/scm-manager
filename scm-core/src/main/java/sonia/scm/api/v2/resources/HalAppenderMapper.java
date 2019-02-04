package sonia.scm.api.v2.resources;

import com.google.common.annotations.VisibleForTesting;

import javax.inject.Inject;

public class HalAppenderMapper {

  @Inject
  private HalEnricherRegistry registry;

  @VisibleForTesting
  void setRegistry(HalEnricherRegistry registry) {
    this.registry = registry;
  }

  protected void appendLinks(HalAppender appender, Object source, Object... contextEntries) {
    // null check is only their to not break existing tests
    if (registry != null) {

      Object[] ctx = new Object[contextEntries.length + 1];
      ctx[0] = source;
      for (int i = 0; i < contextEntries.length; i++) {
        ctx[i + 1] = contextEntries[i];
      }

      HalEnricherContext context = HalEnricherContext.of(ctx);

      Iterable<HalEnricher> enrichers = registry.allByType(source.getClass());
      for (HalEnricher enricher : enrichers) {
        enricher.enrich(context, appender);
      }
    }
  }

}
