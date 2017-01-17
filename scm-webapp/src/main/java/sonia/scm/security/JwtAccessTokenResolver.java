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

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.util.Set;
import javax.inject.Inject;
import org.apache.shiro.authc.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.Extension;

/**
 * Jwt implementation of {@link AccessTokenResolver}.
 * 
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
@Extension
public final class JwtAccessTokenResolver implements AccessTokenResolver {

  /**
   * the logger for JwtAccessTokenResolver
   */
  private static final Logger LOG = LoggerFactory.getLogger(JwtAccessTokenResolver.class);
  
  private final SecureKeyResolver keyResolver;
  private final Set<TokenClaimsValidator> validators;

  @Inject
  public JwtAccessTokenResolver(SecureKeyResolver keyResolver, Set<TokenClaimsValidator> validators) {
    this.keyResolver = keyResolver;
    this.validators = validators;
  }
  
  @Override
  public JwtAccessToken resolve(BearerToken bearerToken) {
    Claims claims;

    try {
      // parse and validate
      claims = Jwts.parser()
        .setSigningKeyResolver(keyResolver)
        .parseClaimsJws(bearerToken.getCredentials())
        .getBody();
      
      // check all registered claims validators
      validators.forEach((validator) -> {
        if (!validator.validate(claims)) {
          LOG.warn("token claims is invalid, marked by validator {}", validator.getClass());
          throw new AuthenticationException("token claims is invalid");
        }
      });
    } catch (JwtException ex) {
      throw new AuthenticationException("signature is invalid", ex);
    }
    
    return new JwtAccessToken(claims, bearerToken.getCredentials());
  }
  
}
