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
import com.google.common.base.Strings;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.SigningKeyResolverAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;

import static com.google.common.base.Preconditions.*;

//~--- JDK imports ------------------------------------------------------------

import java.security.SecureRandom;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Resolve secure keys which can be used for signing token and messages.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
@Singleton
public class SecureKeyResolver extends SigningKeyResolverAdapter
{

  /** key length */
  private static final int KEY_LENGTH = 64;

  /** name of the configuration store */
  @VisibleForTesting
  static final String STORE_NAME = "keys";

  /**
   * the logger for SecureKeyResolver
   */
  private static final Logger logger =
    LoggerFactory.getLogger(SecureKeyResolver.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new SecureKeyResolver
   *
   *
   * @param storeFactory store factory
   */
  @Inject
  public SecureKeyResolver(ConfigurationEntryStoreFactory storeFactory)
  {
    this.store = storeFactory.getStore(SecureKey.class, STORE_NAME);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override
  public byte[] resolveSigningKeyBytes(JwsHeader header, Claims claims)
  {
    checkNotNull(claims, "claims is required");
    
    String subject = claims.getSubject();

    checkArgument(!Strings.isNullOrEmpty(subject), "subject is required");

    SecureKey key = store.get(subject);

    checkState(key != null, "could not resolve key for subject %s", subject);

    return key.getBytes();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the secure key for the given subject, if there is no key for the
   * subject a new key is generated.
   *
   * @param subject subject
   *
   * @return secure key
   */
  public SecureKey getSecureKey(String subject)
  {
    SecureKey key = store.get(subject);

    if (key == null)
    {
      logger.trace("create new key for subject");
      key = createNewKey();
      store.put(subject, key);
    }

    return key;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Creates a new secure key.
   *
   *
   * @return new secure key
   */
  private SecureKey createNewKey()
  {
    byte[] bytes = new byte[KEY_LENGTH];

    random.nextBytes(bytes);

    return new SecureKey(bytes, System.currentTimeMillis());
  }

  //~--- fields ---------------------------------------------------------------

  /** secure randon */
  private final SecureRandom random = new SecureRandom();

  /** configuration entry store */
  private final ConfigurationEntryStore<SecureKey> store;
}
