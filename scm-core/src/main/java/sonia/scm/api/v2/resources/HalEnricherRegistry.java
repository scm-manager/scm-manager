/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
    
package sonia.scm.api.v2.resources;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import jakarta.inject.Singleton;
import sonia.scm.plugin.Extension;

/**
 * The {@link HalEnricherRegistry} is responsible for binding {@link HalEnricher} instances to their source types.
 *
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
   */
  public Iterable<HalEnricher> allByType(Class sourceType) {
    return enrichers.get(sourceType);
  }
}
