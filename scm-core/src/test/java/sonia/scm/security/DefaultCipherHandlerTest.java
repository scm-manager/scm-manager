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

import com.google.common.io.Files;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.SCMContextProvider;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultCipherHandler}.
 *
 */
@ExtendWith({MockitoExtension.class})
class DefaultCipherHandlerTest {

  @Mock
  private SCMContextProvider context;

  @Mock
  private KeyGenerator keyGenerator;

  /**
   * Tests loading and storing default key.
   */
  @Test
  void shouldLoadAndStoreDefaultKey(@TempDir Path tempDir) throws IOException {
    File baseDirectory = tempDir.toFile();

    when(context.getBaseDirectory()).thenReturn(baseDirectory);
    when(keyGenerator.createKey()).thenReturn("secret");

    DefaultCipherHandler cipher = new DefaultCipherHandler(context, keyGenerator);
    File configDirectory = new File(baseDirectory, "config");
    File defaultKeyFile = new File(configDirectory, DefaultCipherHandler.CIPHERKEY_FILENAME);
    assertThat(defaultKeyFile).exists();

    // plain text for assertion
    String plain = "hallo123";

    // encrypt value with new generated key
    String encrypted = cipher.encode(plain);

    // load key from disk
    cipher = new DefaultCipherHandler(context, keyGenerator);

    // decrypt with loaded key
    assertThat(cipher.decode(encrypted)).isEqualTo(plain);
  }

  @Test
  @SuppressWarnings("UnstableApiUsage") // is ok for unit test
  void shouldReEncodeOldFormattedDefaultKey(@TempDir Path tempDir) throws IOException {
    String oldKey = "17eXopruTtX3S4dJ9KTEmbZ-vfZztw==";
    String encryptedValue = "A11kQF7wytpWCkjPflxJB-zUWJ1CVKU3qhwhRFq4Pvl6XqiS9V2w-gqNktqMX6YNDw==";
    String plainValue = "Marvin The Paranoid Android - RAM";

    File baseDirectory = tempDir.toFile();

    when(context.getBaseDirectory()).thenReturn(baseDirectory);

    File configDirectory = new File(baseDirectory, "config");
    configDirectory.mkdirs();
    File defaultKeyFile = new File(configDirectory, DefaultCipherHandler.CIPHERKEY_FILENAME);
    Files.write(oldKey.getBytes(StandardCharsets.UTF_8), defaultKeyFile);


    DefaultCipherHandler cipher = new DefaultCipherHandler(context, keyGenerator);

    String newKey = Files.readLines(defaultKeyFile, StandardCharsets.UTF_8).get(0);
    assertThat(newKey).startsWith(DefaultCipherHandler.PREFIX_FORMAT_V2);
    assertThat(cipher.decode(encryptedValue)).isEqualTo(plainValue);
  }

  /**
   * Test encode and decode method with a separate key.
   */
  @Test
  void shouldEncodeAndDecodeWithSeparateKey(){
    char[] key = "testkey".toCharArray();
    DefaultCipherHandler cipher = new DefaultCipherHandler("somekey");
    assertThat(cipher.decode(key, cipher.encode(key, "hallo123"))).isEqualTo("hallo123");
  }

  /**
   * Test encode and decode method with the default key.
   */
  @Test
  void shouldEncodeAndDecodeWithDefaultKey() {
    DefaultCipherHandler cipher = new DefaultCipherHandler("testkey");
    assertThat(cipher.decode(cipher.encode("hallo123"))).isEqualTo("hallo123");
  }

  @Test
  void shouldDecodeOldCipherFormat() {
    DefaultCipherHandler cipher = new DefaultCipherHandler("hitchhiker-secrets");
    String oldFormat = "zhoCMoApolM3cMPRqXHjcGBd-gDQN0JHwWBxvyh3xnCWzj5V";
    assertThat(cipher.decode(oldFormat)).isEqualTo("Arthur Dent's Secret");
  }

  @Test
  void shouldAddPrefixToEncodedValue() {
    DefaultCipherHandler cipher = new DefaultCipherHandler("hitchhiker-secrets");
    String encoded = cipher.encode("Trillian's Secret Dairy");
    assertThat(encoded).startsWith(DefaultCipherHandler.PREFIX_FORMAT_V2);
  }

}
