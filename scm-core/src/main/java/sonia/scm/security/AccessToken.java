/**
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
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
public interface AccessToken {

  /**
   * Returns unique id of the access token.
   *
   * @return unique id
   */
  String getId();

  /**
   * Returns name of subject which identifies the principal.
   *
   * @return name of subject
   */
  String getSubject();

  /**
   * Returns optional issuer. The issuer identifies the principal that issued the token.
   *
   * @return optional issuer
   */
  Optional<String> getIssuer();

  /**
   * Returns time at which the token was issued.
   *
   * @return time at which the token was issued
   */
  Date getIssuedAt();

  /**
   * Returns the expiration time of token.
   *
   * @return expiration time
   */
  Date getExpiration();

  /**
   * Returns refresh expiration of token.
   *
   * @return refresh expiration
   */
  Optional<Date> getRefreshExpiration();

  /**
   * Returns id of the parent key.
   *
   * @return parent key id
   */
  Optional<String> getParentKey();

  /**
   * Returns the scope of the token. The scope is able to reduce the permissions of the subject in the context of this
   * token. For example we could issue a token which can only be used to read a single repository. for more informations
   * please have a look at {@link Scope}.
   *
   * @return scope of token.
   */
  Scope getScope();

  /**
   * Returns name of groups, in which the user should be a member.
   *
   * @return name of groups
   */
  Set<String> getGroups();

  /**
   * Returns an optional value of a custom token field.
   *
   * @param <T> type of field
   * @param key key of token field
   *
   * @return optional value of custom field
   */
  <T> Optional<T> getCustom(String key);

  /**
   * Returns compact representation of token.
   *
   * @return compact representation
   */
  String compact();

  /**
   * Returns read only map of all claim keys with their values.
   */
  Map<String, Object> getClaims();
}
