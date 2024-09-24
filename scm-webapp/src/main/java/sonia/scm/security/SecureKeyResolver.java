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
 * @since 2.0.0
 */
@Singleton
public class SecureKeyResolver extends SigningKeyResolverAdapter
{

  private static final int KEY_LENGTH = 64;

  /** name of the configuration store */
  @VisibleForTesting
  static final String STORE_NAME = "keys";

 
  private static final Logger logger =
    LoggerFactory.getLogger(SecureKeyResolver.class);

  /** secure randon */
  private final Random random;

  /** configuration entry store */
  private final ConfigurationEntryStore<SecureKey> store;

  private final JwtSettingsStore jwtSettingsStore;

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


 
  @Override
  public byte[] resolveSigningKeyBytes(JwsHeader header, Claims claims)
  {
    checkNotNull(claims, "claims is required");
    
    String subject = claims.getSubject();

    checkArgument(!Strings.isNullOrEmpty(subject), "subject is required");

    return getSecureKey(subject).getBytes();
  }


  /**
   * Returns the secure key for the given subject, if there is no key for the
   * subject a new key is generated.
   *
   * @param subject subject
   *
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

  private SecureKey createNewKey()
  {
    byte[] bytes = new byte[KEY_LENGTH];

    random.nextBytes(bytes);

    return new SecureKey(bytes, System.currentTimeMillis());
  }

}
