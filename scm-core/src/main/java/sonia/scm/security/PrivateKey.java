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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Can be used to create signatures of data.
 * @since 2.4.0
 */
public interface PrivateKey {

  /**
   * Returns the key's id.
   */
  String getId();

  /**
   * Creates a signature for the given data.
   * @param stream data stream to sign
   * @return signature
   */
  byte[] sign(InputStream stream);

  /**
   * Creates a signature for the given data.
   * @param data data to sign
   * @return signature
   */
  default byte[] sign(byte[] data) {
    return sign(new ByteArrayInputStream(data));
  }
}
