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

package sonia.scm.importexport;

import com.google.common.io.ByteSource;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RepositoryImportExportEncryptionTest {

  private final RepositoryImportExportEncryption encryption = new RepositoryImportExportEncryption();

  @Test
  void shouldNotEncryptWithoutPassword() throws IOException {
    String content = "my content";
    String secret = "";
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    OutputStream os = encryption.optionallyEncrypt(baos, secret);
    os.write(content.getBytes());
    os.flush();

    assertThat(os).hasToString(content);
  }

  @Test
  void shouldNotDecryptWithoutPassword() throws IOException {
    String content = "my content";
    String secret = "";
    ByteArrayInputStream bais = new ByteArrayInputStream(content.getBytes());

    InputStream is = encryption.optionallyDecrypt(bais, secret);

    ByteSource byteSource = new ByteSource() {
      @Override
      public InputStream openStream() {
        return is;
      }
    };

    String result = byteSource.asCharSource(StandardCharsets.UTF_8).read();

    assertThat(result).isEqualTo(content);
  }

  @Test
  void shouldEncryptAndDecryptContentWithPassword() throws IOException {
    String content = "my content";
    String secret = "secretPassword";
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    OutputStream os = encryption.optionallyEncrypt(baos, secret);
    os.write(content.getBytes());
    os.flush();
    os.close();

    assertThat(baos.toString()).isNotEqualTo(content);

    InputStream is = encryption.optionallyDecrypt(new ByteArrayInputStream(baos.toByteArray()), secret);
    ByteSource byteSource = new ByteSource() {
      @Override
      public InputStream openStream() {
        return is;
      }
    };
    String result = byteSource.asCharSource(StandardCharsets.UTF_8).read();

    assertThat(result).isEqualTo(content);
  }

  @Test
  void shouldFailOnDecryptIfNotEncrypted() {
    String content = "my content";
    String secret = "secretPassword";

    ByteArrayInputStream notEncryptedStream = new ByteArrayInputStream(content.getBytes());

    assertThrows(IOException.class, () -> encryption.optionallyDecrypt(notEncryptedStream, secret));
  }
}
