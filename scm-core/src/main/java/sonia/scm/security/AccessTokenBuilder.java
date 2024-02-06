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

import java.util.concurrent.TimeUnit;

/**
 * The access token builder is able to create {@link AccessToken}. For more informations about access tokens have look
 * at {@link AccessToken}.
 * 
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
   * @param unit expiration unit
   * 
   * @return {@code this}
   */
  AccessTokenBuilder expiresIn(long count, TimeUnit unit);

  /**
   * Sets the time how long this token may be refreshed. Set this to 0 (zero) to disable automatic refresh.
   *
   * @param count Time unit count. If set to 0, automatic refresh is disabled.
   * @param unit time unit
   *
   * @return {@code this}
   */
  AccessTokenBuilder refreshableFor(long count, TimeUnit unit);

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
