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
