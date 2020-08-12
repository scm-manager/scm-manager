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

package sonia.scm.security.gpg;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPKeyRingGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sonia.scm.util.MockUtil;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

import static org.assertj.core.api.Assertions.assertThat;

class GPGKeyExporterTest {

  private static void registerBouncyCastleProviderIfNecessary() {
    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
      Security.addProvider(new BouncyCastleProvider());
    }
  }

  @AfterEach
  void unbindThreadContext() {
    ThreadContext.unbindSubject();
    ThreadContext.unbindSecurityManager();
  }

  @BeforeEach
  void bindThreadContext() {
    registerBouncyCastleProviderIfNecessary();

    SecurityUtils.setSecurityManager(new DefaultSecurityManager());
    ThreadContext.bind(MockUtil.createUserSubject(SecurityUtils.getSecurityManager()));
  }

  @Test
  void shouldExportGeneratedKeyPair() throws NoSuchProviderException, NoSuchAlgorithmException, PGPException, IOException {
    final PGPKeyRingGenerator keyRingGenerator = GPGKeyPairGenerator.generateKeyPair();

    final String exportedPublicKey = GPGKeyExporter.exportKeyRing(keyRingGenerator.generatePublicKeyRing());
    assertThat(exportedPublicKey)
      .isNotBlank()
      .startsWith("-----BEGIN PGP PUBLIC KEY BLOCK-----")
      .contains("-----END PGP PUBLIC KEY BLOCK-----");

    final String exportedPrivateKey = GPGKeyExporter.exportKeyRing(keyRingGenerator.generateSecretKeyRing());
    assertThat(exportedPrivateKey)
      .isNotBlank()
      .startsWith("-----BEGIN PGP PRIVATE KEY BLOCK-----")
      .contains("-----END PGP PRIVATE KEY BLOCK-----");
  }

}
