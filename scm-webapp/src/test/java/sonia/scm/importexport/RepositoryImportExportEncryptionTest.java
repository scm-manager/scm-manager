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
import static sonia.scm.importexport.RepositoryImportExportEncryption.decrypt;

class RepositoryImportExportEncryptionTest {

  @Test
  void shouldNotEncryptWithoutPassword() throws IOException {
    String content = "my content";
    String secret = "";
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    OutputStream os = RepositoryImportExportEncryption.encrypt(baos, secret);
    os.write(content.getBytes());
    os.flush();

    assertThat(os.toString()).isEqualTo(content);
  }

  @Test
  void shouldNotDecryptWithoutPassword() throws IOException {
    String content = "my content";
    String secret = "";
    ByteArrayInputStream bais = new ByteArrayInputStream(content.getBytes());

    InputStream is = RepositoryImportExportEncryption.decrypt(bais, secret);

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

    OutputStream os = RepositoryImportExportEncryption.encrypt(baos, secret);
    os.write(content.getBytes());
    os.flush();

    assertThat(os.toString()).isNotEqualTo(content);

    InputStream is = decrypt(new ByteArrayInputStream(baos.toByteArray()), secret);

    ByteSource byteSource = new ByteSource() {
      @Override
      public InputStream openStream() {
        return is;
      }
    };

    String result = byteSource.asCharSource(StandardCharsets.UTF_8).read();

    assertThat(result).isEqualTo(content);
  }
}
