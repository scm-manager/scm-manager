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
    
package sonia.scm.security;

import java.time.Clock;
import java.util.Set;
import javax.inject.Inject;
import sonia.scm.plugin.Extension;

/**
 * Jwt implementation of {@link AccessTokenBuilderFactory}.
 * 
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
@Extension
public final class JwtAccessTokenBuilderFactory implements AccessTokenBuilderFactory {

  private final KeyGenerator keyGenerator;
  private final SecureKeyResolver keyResolver;
  private final Set<AccessTokenEnricher> enrichers;
  private final Clock clock;

  @Inject
  public JwtAccessTokenBuilderFactory(
    KeyGenerator keyGenerator, SecureKeyResolver keyResolver, Set<AccessTokenEnricher> enrichers) {
    this(keyGenerator, keyResolver, enrichers, Clock.systemDefaultZone());
  }

  JwtAccessTokenBuilderFactory(
    KeyGenerator keyGenerator, SecureKeyResolver keyResolver, Set<AccessTokenEnricher> enrichers, Clock clock) {
    this.keyGenerator = keyGenerator;
    this.keyResolver = keyResolver;
    this.enrichers = enrichers;
    this.clock = clock;
  }

  @Override
  public JwtAccessTokenBuilder create() {
    JwtAccessTokenBuilder builder = new JwtAccessTokenBuilder(keyGenerator, keyResolver, clock);
    
    // enrich access token builder
    enrichers.forEach((enricher) -> {
      enricher.enrich(builder);
    });
    
    return builder;
  }

}
