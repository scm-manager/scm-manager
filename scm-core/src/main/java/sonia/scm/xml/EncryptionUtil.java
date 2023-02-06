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

package sonia.scm.xml;

import com.google.common.base.Strings;
import sonia.scm.security.CipherUtil;

public final class EncryptionUtil {

  private static final String PREFIX = "{enc}";

  private EncryptionUtil() {
  }

  public static String decrypt(String value) {
    if (!value.startsWith(PREFIX)) {
      //Return value if not encrypted yet
      return value;
    }

    return CipherUtil.getInstance().decode(value.substring(PREFIX.length()));
  }

  public static String encrypt(String value) {
    return PREFIX.concat(CipherUtil.getInstance().encode(value));
  }

  public static boolean isEncrypted(String value) {
    return !Strings.isNullOrEmpty(value) && value.startsWith(PREFIX);
  }
}
