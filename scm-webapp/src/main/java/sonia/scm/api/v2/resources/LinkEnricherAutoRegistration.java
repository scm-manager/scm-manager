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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.Extension;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Set;

/**
 * Registers every {@link HalEnricher} which is annotated with an {@link Enrich} annotation.
 */
@Extension
public class LinkEnricherAutoRegistration implements ServletContextListener {

  private static final Logger LOG = LoggerFactory.getLogger(LinkEnricherAutoRegistration.class);

  private final HalEnricherRegistry registry;
  private final Set<HalEnricher> enrichers;

  @Inject
  public LinkEnricherAutoRegistration(HalEnricherRegistry registry, Set<HalEnricher> enrichers) {
    this.registry = registry;
    this.enrichers = enrichers;
  }

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    for (HalEnricher enricher : enrichers) {
      Enrich annotation = enricher.getClass().getAnnotation(Enrich.class);
      if (annotation != null) {
        registry.register(annotation.value(), enricher);
      } else {
        LOG.warn("found HalEnricher extension {} without Enrich annotation", enricher.getClass());
      }
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    // nothing todo
  }
}
