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

import sonia.scm.repository.Person;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * The public key can be used to verify signatures.
 *
 * @since 2.4.0
 */
public interface PublicKey {

  /**
   * Returns id of the public key.
   */
  String getId();

  /**
   * Returns ids from gpg sub keys.
   *
   * @since 2.19.0
   */
  default Set<String> getSubkeys() {
    return Collections.emptySet();
  }

  /**
   * Returns the username of the owner or an empty optional.
   */
  Optional<String> getOwner();

  /**
   * Returns raw of the public key.
   */
  String getRaw();

  /**
   * Returns the contacts of the publickey.
   *
   * @return owner or empty optional
   */
  Set<Person> getContacts();

  /**
   * Verifies that the signature is valid for the given data.
   *
   * @param stream    stream of data to verify
   * @param signature signature
   * @return {@code true} if the signature is valid for the given data
   */
  boolean verify(InputStream stream, byte[] signature);

  /**
   * Verifies that the signature is valid for the given data.
   *
   * @param data      data to verify
   * @param signature signature
   * @return {@code true} if the signature is valid for the given data
   */
  default boolean verify(byte[] data, byte[] signature) {
    return verify(new ByteArrayInputStream(data), signature);
  }
}
