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

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.util.ThreadContext;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pgpainless.PGPainless;
import sonia.scm.util.MockUtil;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
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
  void shouldExportGeneratedKeyPair() throws NoSuchAlgorithmException, PGPException, IOException, InvalidAlgorithmParameterException {
    final PGPSecretKeyRing secretKeys = GPGKeyPairGenerator.generateKeyPair();

    final String exportedPublicKey = PGPainless.asciiArmor(PGPainless.extractCertificate(secretKeys));
    assertThat(exportedPublicKey)
      .isNotBlank()
      .startsWith("-----BEGIN PGP PUBLIC KEY BLOCK-----")
      .contains("-----END PGP PUBLIC KEY BLOCK-----");

    final String exportedPrivateKey = PGPainless.asciiArmor(secretKeys);
    assertThat(exportedPrivateKey)
      .isNotBlank()
      .startsWith("-----BEGIN PGP PRIVATE KEY BLOCK-----")
      .contains("-----END PGP PRIVATE KEY BLOCK-----");
  }

}
