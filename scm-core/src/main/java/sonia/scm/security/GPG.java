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

import java.util.Optional;

/**
 * Allows signing and verification using gpg.
 *
 * @since 2.3.0
 */
public interface GPG {

  /**
   * Returns the id of the key from the given signature.
   * @param signature signature
   * @return public key id
   */
  String findPublicKeyId(byte[] signature);

  /**
   * Returns the public key with the given id or an empty optional.
   * @param id id of public
   * @return public key or empty optional
   */
  Optional<PublicKey> findPublicKey(String id);

  /**
   * Returns all public keys assigned to the given username
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
