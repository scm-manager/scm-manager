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

import java.util.concurrent.TimeUnit;

/**
 * The access token builder is able to create {@link AccessToken}. For more informations about access tokens have look
 * at {@link AccessToken}.
 * 
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
public interface AccessTokenBuilder {
  
  /**
   * Sets the subject for the token. 
   * If the subject is not set the currently authenticated subject will be used instead.
   * 
   * @param subject subject of token
   * 
   * @return * @return {@code this}
   */
  AccessTokenBuilder subject(String subject);
  
  /**
   * Adds a custom entry to the token.
   * 
   * @param key key of custom entry
   * @param value value of entry
   * 
   * @return {@code this}
   */
  AccessTokenBuilder custom(String key, Object value);
  
  /**
   * Sets the issuer for the token.
   * 
   * @param issuer issuer name or url
   * 
   * @return {@code this}
   */
  AccessTokenBuilder issuer(String issuer);
  
  /**
   * Sets the expiration for the token.
   * 
   * @param count expiration count
   * @param unit expirtation unit
   * 
   * @return {@code this}
   */
  AccessTokenBuilder expiresIn(long count, TimeUnit unit);
  
  /**
   * Reduces the permissions of the token by providing a scope.
   * 
   * @param scope scope of token
   * 
   * @return {@code this}
   */
  AccessTokenBuilder scope(Scope scope);
  
  /**
   * Creates a new {@link AccessToken} with the provided settings.
   * 
   * @return new {@link AccessToken}
   */
  AccessToken build();
  
}
