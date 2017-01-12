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

import com.google.common.annotations.VisibleForTesting;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.apache.shiro.realm.AuthenticatingRealm;

import sonia.scm.group.GroupDAO;
import sonia.scm.plugin.Extension;
import sonia.scm.user.UserDAO;

import static com.google.common.base.Preconditions.checkArgument;
import java.util.Set;

//~--- JDK imports ------------------------------------------------------------

import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Realm for authentication with {@link BearerAuthenticationToken}.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
@Singleton
@Extension
public class BearerRealm extends AuthenticatingRealm
{

  /**
   * the logger for BearerRealm
   */
  private static final Logger LOG = LoggerFactory.getLogger(BearerRealm.class);
  
  /** realm name */
  @VisibleForTesting
  static final String REALM = "BearerRealm";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param resolver key resolver
   * @param userDAO user dao
   * @param groupDAO group dao
   * @param validators token claims validators
   */
  @Inject
  public BearerRealm(SecureKeyResolver resolver, UserDAO userDAO,
    GroupDAO groupDAO, Set<TokenClaimsValidator> validators)
  {
    this.resolver = resolver;
    this.helper = new DAORealmHelper(REALM, userDAO, groupDAO);
    this.validators = validators;
    
    setCredentialsMatcher(new AllowAllCredentialsMatcher());
    setAuthenticationTokenClass(BearerAuthenticationToken.class);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Validates the given jwt token and retrieves authentication data from
   * {@link UserDAO} and {@link GroupDAO}.
   *
   *
   * @param token jwt token
   *
   * @return authentication data from user and group dao
   */
  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(
    AuthenticationToken token)
  {
    checkArgument(token instanceof BearerAuthenticationToken, "%s is required",
      BearerAuthenticationToken.class);

    BearerAuthenticationToken bt = (BearerAuthenticationToken) token;
    Claims c = checkToken(bt);

    return helper.getAuthenticationInfo(c.getSubject(), bt.getCredentials());
  }

  /**
   * Validates the jwt token.
   *
   *
   * @param token jwt token
   *
   * @return claim
   */
  private Claims checkToken(BearerAuthenticationToken token)
  {
    Claims claims;

    try
    {
      //J-
      claims = Jwts.parser()
        .setSigningKeyResolver(resolver)
        .parseClaimsJws(token.getCredentials())
        .getBody();
      //J+
      
      // check all registered claims validators
      validators.forEach((validator) -> {
        if (!validator.validate(claims)) {
          LOG.warn("token claims is invalid, marked by validator {}", validator.getClass());
          throw new AuthenticationException("token claims is invalid");
        }
      });
    }
    catch (JwtException ex)
    {
      throw new AuthenticationException("signature is invalid", ex);
    }

    return claims;
  }

  //~--- fields ---------------------------------------------------------------

  /** token claims validators **/
  private final Set<TokenClaimsValidator> validators;
  
  /** dao realm helper */
  private final DAORealmHelper helper;

  /** secure key resolver */
  private final SecureKeyResolver resolver;
}
