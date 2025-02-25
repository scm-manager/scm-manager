/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.security;

import jakarta.inject.Inject;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.plugin.Extension;

import java.time.Clock;
import java.util.Set;

/**
 * Jwt implementation of {@link AccessTokenBuilderFactory}.
 * 
 * @since 2.0.0
 */
@Extension
public final class JwtAccessTokenBuilderFactory implements AccessTokenBuilderFactory {

  private final KeyGenerator keyGenerator;
  private final SecureKeyResolver keyResolver;
  private final JwtConfig jwtConfig;
  private final Set<AccessTokenEnricher> enrichers;
  private final Clock clock;
  private final ScmConfiguration scmConfiguration;

  @Inject
  public JwtAccessTokenBuilderFactory(
    KeyGenerator keyGenerator, SecureKeyResolver keyResolver, Set<AccessTokenEnricher> enrichers, JwtConfig jwtConfig, ScmConfiguration scmConfiguration) {
    this(keyGenerator, keyResolver, jwtConfig, enrichers, Clock.systemDefaultZone(), scmConfiguration);
  }

  JwtAccessTokenBuilderFactory(
    KeyGenerator keyGenerator, SecureKeyResolver keyResolver, JwtConfig jwtConfig, Set<AccessTokenEnricher> enrichers, Clock clock, ScmConfiguration scmConfiguration) {
    this.keyGenerator = keyGenerator;
    this.keyResolver = keyResolver;
    this.jwtConfig = jwtConfig;
    this.enrichers = enrichers;
    this.clock = clock;
    this.scmConfiguration = scmConfiguration;
  }

  @Override
  public JwtAccessTokenBuilder create() {
    JwtAccessTokenBuilder builder = new JwtAccessTokenBuilder(keyGenerator, keyResolver, jwtConfig, clock, scmConfiguration);
    
    // enrich access token builder
    enrichers.forEach((enricher) -> {
      enricher.enrich(builder);
    });
    
    return builder;
  }

}
