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

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * An access token can be used to access scm-manager without providing username and password. An {@link AccessToken} can
 * be issued from a restful webservice endpoint by providing credentials. After the token was issued, the token must be
 * send along with every request. The token should be send in its compact representation as bearer authorization header
 * or as cookie.
 *
 * @since 2.0.0
 */
public interface AccessToken {

  /**
   * Returns unique id of the access token.
   */
  String getId();

  /**
   * Returns name of subject which identifies the principal.
   */
  String getSubject();

  /**
   * Returns optional issuer. The issuer identifies the principal that issued the token.
   */
  Optional<String> getIssuer();

  /**
   * Returns time at which the token was issued.
   */
  Date getIssuedAt();

  /**
   * Returns the expiration time of token.
   */
  Date getExpiration();

  /**
   * Returns refresh expiration of token.
   */
  Optional<Date> getRefreshExpiration();

  /**
   * Returns id of the parent key.
   */
  Optional<String> getParentKey();

  /**
   * Returns the scope of the token. The scope is able to reduce the permissions of the subject in the context of this
   * token. For example, we could issue a token which can only be used to read a single repository. for more informations
   * please have a look at {@link Scope}.
   */
  Scope getScope();

  /**
   * Returns name of groups, in which the user should be a member.
   */
  Set<String> getGroups();

  /**
   * Returns an optional value of a custom token field.
   *
   * @param <T> type of field
   * @param key key of token field
   */
  <T> Optional<T> getCustom(String key);

  /**
   * Returns compact representation of token.
   */
  String compact();

  /**
   * Returns read only map of all claim keys with their values.
   */
  Map<String, Object> getClaims();
}
