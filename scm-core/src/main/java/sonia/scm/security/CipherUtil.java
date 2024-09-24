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

import sonia.scm.SCMContext;
import sonia.scm.util.ServiceUtil;

/**
 *
 * @since 1.7
 */
public final class CipherUtil {

  private static volatile CipherUtil instance;

  private CipherHandler cipherHandler;
  private KeyGenerator keyGenerator;

  private CipherUtil() {
    keyGenerator = ServiceUtil.getService(KeyGenerator.class);

    if (keyGenerator == null) {
      keyGenerator = new UUIDKeyGenerator();
    }

    cipherHandler = ServiceUtil.getService(CipherHandler.class);

    if (cipherHandler == null) {
      cipherHandler = new DefaultCipherHandler(SCMContext.getContext(), keyGenerator);
    }
  }

  public static CipherUtil getInstance() {
    if (instance == null) {
      synchronized (CipherUtil.class) {
        if (instance == null) {
          instance = new CipherUtil();
        }
      }
    }

    return instance;
  }

  public String decode(String value) {
    return cipherHandler.decode(value);
  }

  public byte[] decode(byte[] value) {
    return cipherHandler.decode(value);
  }

  public String encode(String value) {
    return cipherHandler.encode(value);
  }

  public byte[] encode(byte[] value) {
    return cipherHandler.encode(value);
  }

  public CipherHandler getCipherHandler()
  {
    return cipherHandler;
  }

  public KeyGenerator getKeyGenerator()
  {
    return keyGenerator;
  }
}
