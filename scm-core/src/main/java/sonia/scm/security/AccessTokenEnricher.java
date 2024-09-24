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

import sonia.scm.plugin.ExtensionPoint;

/**
 * AccessTokenEnricher is able to enhance the {@link AccessToken}, before it is delivered to the client. 
 * AccessTokenEnricher can be used to add custom fields to the {@link AccessToken}. The enricher is always called before
 * an {@link AccessToken} is build by the {@link AccessTokenBuilder}.
 * 
 * @since 2.0.0
 */
@ExtensionPoint
public interface AccessTokenEnricher {
  
  /**
   * Enriches the access token by adding fields to the {@link AccessTokenBuilder}.
   * 
   * @param builder access token builder
   */
  void enrich(AccessTokenBuilder builder);
}
