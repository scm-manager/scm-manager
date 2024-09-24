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
