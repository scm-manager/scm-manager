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

import java.util.Optional;

/**
 * Allows signing and verification using gpg.
 *
 * @since 2.4.0
 */
public interface GPG {

  /**
   * Returns the id of the key from the given signature.
   *
   * @param signature signature
   * @return public key id
   */
  String findPublicKeyId(byte[] signature);

  /**
   * Returns the public key with the given id or an empty optional.
   *
   * @param id id of public
   * @return public key or empty optional
   */
  Optional<PublicKey> findPublicKey(String id);

  /**
   * Returns all public keys assigned to the given username
   *
   * @param username username of the public key owner
   * @return collection of public keys
   */
  Iterable<PublicKey> findPublicKeysByUsername(String username);

  /**
   * Returns the default private key of the currently authenticated user.
   *
   * @return default private key
   */
  PrivateKey getPrivateKey();
}
