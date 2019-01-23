package sonia.scm.api.v2.resources;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import sonia.scm.plugin.Extension;

import javax.inject.Singleton;

/**
 * The {@link LinkEnricherRegistry} is responsible for binding {@link LinkEnricher} instances to their source types.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
@Extension
@Singleton
public final class LinkEnricherRegistry {

  private final Multimap<Class, LinkEnricher> enrichers = HashMultimap.create();

  /**
   * Registers a new {@link LinkEnricher} for the given source type.
   *
   * @param sourceType type of json mapping source
   * @param enricher link enricher instance
   */
  public void register(Class sourceType, LinkEnricher enricher) {
    enrichers.put(sourceType, enricher);
  }

  /**
   * Returns all registered {@link LinkEnricher} for the given type.
   *
   * @param sourceType type of json mapping source
   * @return all registered enrichers
   */
  public Iterable<LinkEnricher> allByType(Class sourceType) {
    return enrichers.get(sourceType);
  }
}
