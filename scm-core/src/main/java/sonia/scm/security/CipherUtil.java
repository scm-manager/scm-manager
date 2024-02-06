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
