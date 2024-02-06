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
