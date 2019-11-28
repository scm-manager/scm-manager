package sonia.scm.api.v2.resources;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import sonia.scm.plugin.Extension;

import javax.inject.Singleton;

/**
 * The {@link HalEnricherRegistry} is responsible for binding {@link HalEnricher} instances to their source types.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
@Extension
@Singleton
public final class HalEnricherRegistry {

  private final Multimap<Class, HalEnricher> enrichers = HashMultimap.create();

  /**
   * Registers a new {@link HalEnricher} for the given source type.
   *
   * @param sourceType type of json mapping source
   * @param enricher link enricher instance
   */
  public void register(Class sourceType, HalEnricher enricher) {
    enrichers.put(sourceType, enricher);
  }

  /**
   * Returns all registered {@link HalEnricher} for the given type.
   *
   * @param sourceType type of json mapping source
   * @return all registered enrichers
   */
  public Iterable<HalEnricher> allByType(Class sourceType) {
    return enrichers.get(sourceType);
  }
}
