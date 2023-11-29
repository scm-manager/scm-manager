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

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.SigningKeyResolverAdapter;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.lifecycle.jwt.JwtSettings;
import sonia.scm.lifecycle.jwt.JwtSettingsStore;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;

import java.security.SecureRandom;
import java.util.Random;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

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
  @SuppressWarnings("unchecked")
  public SecureKeyResolver(ConfigurationEntryStoreFactory storeFactory, JwtSettingsStore jwtSettingsStore) {
    this(storeFactory, jwtSettingsStore, new SecureRandom());
  }

  SecureKeyResolver(ConfigurationEntryStoreFactory storeFactory, JwtSettingsStore jwtSettingsStore, Random random)
  {
    store = storeFactory
      .withType(SecureKey.class)
      .withName(STORE_NAME)
      .build();
    this.jwtSettingsStore = jwtSettingsStore;
    this.random = random;
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

    return getSecureKey(subject).getBytes();
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

    if (key == null || isKeyExpired(key))
    {
      logger.trace("create new key for subject");
      key = createNewKey();
      store.put(subject, key);
    }

    return key;
  }

  private boolean isKeyExpired(SecureKey key) {
    JwtSettings settings = jwtSettingsStore.get();

    return key.getCreationDate() < settings.getKeysValidAfterTimestampInMs();
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
  private final Random random;

  /** configuration entry store */
  private final ConfigurationEntryStore<SecureKey> store;

  private final JwtSettingsStore jwtSettingsStore;
}
