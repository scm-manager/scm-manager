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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EncryptionUtilTest {

  private final static String API_TOKEN = "113bb79d12c179301b93e9ff1ad32181a0";

  @Test
  void shouldEncrypt() {
    String encryptedToken = EncryptionUtil.encrypt(API_TOKEN);

    assertThat(API_TOKEN).isNotEqualTo(encryptedToken);
    assertThat(encryptedToken).startsWith("{enc}");
  }

  @Test
  void shouldReturnDecryptedApiToken() {
    String encryptedToken = EncryptionUtil.encrypt(API_TOKEN);
    String decryptedToken = EncryptionUtil.decrypt(encryptedToken);

    assertThat(decryptedToken).isEqualTo(API_TOKEN);
  }

  @Test
  void shouldReturnApiTokenIfNotEncrypted() {
    String token = EncryptionUtil.decrypt(API_TOKEN);

    assertThat(token).isEqualTo(API_TOKEN);
  }

  @Test
  void shouldCheckIfTokenIsEncrypted() {
    String encryptedToken = EncryptionUtil.encrypt(API_TOKEN);
    boolean encrypted = EncryptionUtil.isEncrypted(encryptedToken);
    boolean notEncrypted = EncryptionUtil.isEncrypted(API_TOKEN);

    assertThat(encrypted).isEqualTo(true);
    assertThat(notEncrypted).isEqualTo(false);
  }
}
