/**
 * Copyright (c) 2014, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */
package sonia.scm.security;

import java.util.Date;
import java.util.Optional;

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
   * Returns the scope of the token. The scope is able to reduce the permissions of the subject in the context of this 
   * token. For example we could issue a token which can only be used to read a single repository. for more informations
   * please have a look at {@link Scope}.
   * 
   * @return scope of token.
   */
  Scope getScope();
  
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
}
