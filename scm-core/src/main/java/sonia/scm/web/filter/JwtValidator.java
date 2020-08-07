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

package sonia.scm.web.filter;

import java.time.Instant;
import java.util.Base64;

public class JwtValidator {

  private JwtValidator() {
  }

  /**
   * Checks if the jwt token is expired.
   *
   * @return {@code true}if the token is expired
   */
  public static boolean isJwtTokenExpired(String raw) {

    boolean expired = false;

    String[] parts = raw.split("\\.");

    if (parts.length > 1) {
      Base64.Decoder decoder = Base64.getUrlDecoder();
      String payload = new String(decoder.decode(parts[1]));
      String[] splitJwt = payload.split(",");

      for (String entry : splitJwt) {
        if (entry.contains("\"exp\"")) {
          long expirationTime = Long.parseLong(entry.replaceAll("[^\\d.]", ""));

          if (Instant.now().isAfter(Instant.ofEpochSecond(expirationTime))) {
            expired = true;
          }
        }
      }
    }
    return expired;
  }
}
