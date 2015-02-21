/**
 * Copyright (c) 2014, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.security;

//~--- non-JDK imports --------------------------------------------------------

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import sonia.scm.user.User;

import static com.google.common.base.Preconditions.*;

//~--- JDK imports ------------------------------------------------------------

import javax.inject.Inject;

/**
 * Creates bearer token for a given user.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
public final class BearerTokenGenerator
{

  /**
   * Constructs a new token generator.
   *
   *
   * @param keyGenerator key generator
   * @param keyResolver secure key resolver
   */
  @Inject
  public BearerTokenGenerator(KeyGenerator keyGenerator,
    SecureKeyResolver keyResolver)
  {
    this.keyGenerator = keyGenerator;
    this.keyResolver = keyResolver;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Creates a new bearer token for the given user.
   *
   *
   * @param user user
   *
   * @return bearer token
   */
  public String createBearerToken(User user)
  {
    checkNotNull(user, "user is required");

    SecureKey key = keyResolver.getSecureKey(user.getName());

    // TODO add expiration date
    
    //J-
    return Jwts.builder()
      .setSubject(user.getName())
      .setId(keyGenerator.createKey())
      .signWith(SignatureAlgorithm.HS256, key.getBytes())
      .compact();
    //J+
  }

  //~--- fields ---------------------------------------------------------------

  /** key generator */
  private final KeyGenerator keyGenerator;

  /** secure key resolver */
  private final SecureKeyResolver keyResolver;
}
