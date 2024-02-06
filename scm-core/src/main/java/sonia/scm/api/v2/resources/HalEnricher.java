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

import sonia.scm.plugin.ExtensionPoint;

/**
 * A {@link HalEnricher} can be used to append hal specific attributes, such as links, to the json response.
 * To register an enricher use the {@link Enrich} annotation or the {@link HalEnricherRegistry} which is available
 * via injection.
 *
 * <b>Warning:</b> enrichers are always registered as singletons.
 *
 * @since 2.0.0
 */
@ExtensionPoint
@FunctionalInterface
public interface HalEnricher {

  /**
   * Enriches the response with hal specific attributes.
   *
   * @param context contains the source for the json mapping and related objects
   * @param appender can be used to append links or embedded objects to the json response
   */
  void enrich(HalEnricherContext context, HalAppender appender);
}
