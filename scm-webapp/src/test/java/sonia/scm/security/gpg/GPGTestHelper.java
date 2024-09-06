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

package sonia.scm.security.gpg;

import com.google.common.io.Resources;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

final class GPGTestHelper {

  private GPGTestHelper() {
  }

  @SuppressWarnings("UnstableApiUsage")
  static byte[] readResourceAsBytes(String fileName) throws IOException {
    URL resource = Resources.getResource("sonia/scm/security/gpg/" + fileName);
    return Resources.toByteArray(resource);
  }

  @SuppressWarnings("UnstableApiUsage")
  static String readResourceAsString(String fileName) throws IOException {
    URL resource = Resources.getResource("sonia/scm/security/gpg/" + fileName);
    return Resources.toString(resource, StandardCharsets.UTF_8);
  }

}
