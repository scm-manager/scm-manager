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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pgpainless.PGPainless;
import sonia.scm.repository.Person;
import sonia.scm.security.PrivateKey;
import sonia.scm.security.PublicKey;
import sonia.scm.util.MockUtil;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultGPGTest {

  private static void registerBouncyCastleProviderIfNecessary() {
    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
      Security.addProvider(new BouncyCastleProvider());
    }
  }

  @Mock
  private PublicKeyStore publicKeyStore;

  @Mock
  private PrivateKeyStore privateKeyStore;

  @InjectMocks
  private DefaultGPG gpg;

  @AfterEach
  void unbindThreadContext() {
    ThreadContext.unbindSubject();
    ThreadContext.unbindSecurityManager();
  }

  @BeforeEach
  void bindThreadContext() {
    registerBouncyCastleProviderIfNecessary();

    SecurityUtils.setSecurityManager(new DefaultSecurityManager());
    Subject subjectUnderTest = MockUtil.createUserSubject(SecurityUtils.getSecurityManager());
    ThreadContext.bind(subjectUnderTest);
  }

  @Test
  void shouldFindIdInSignature() throws IOException {
    String raw = GPGTestHelper.readResourceAsString("slarti.txt.asc");
    String publicKeyId = gpg.findPublicKeyId(raw.getBytes());

    assertThat(publicKeyId).isEqualTo("0x247E908C6FD35473");
  }

  @Test
  void shouldFindPublicKey() throws IOException {
    String raw = GPGTestHelper.readResourceAsString("subkeys.asc");
    Person trillian = Person.toPerson("Trillian <tricia.mcmillan@hitchhiker.org>");
    RawGpgKey key1 = new RawGpgKey("42", "key_42", "trillian", raw, ImmutableSet.of(trillian), Instant.now());

    when(publicKeyStore.findById("42")).thenReturn(Optional.of(key1));

    Optional<PublicKey> publicKey = gpg.findPublicKey("42");

    assertThat(publicKey).isPresent();
    assertThat(publicKey.get().getOwner()).isPresent();
    assertThat(publicKey.get().getOwner().get()).contains("trillian");
    assertThat(publicKey.get().getId()).isEqualTo("42");
    assertThat(publicKey.get().getContacts()).contains(trillian);
  }

  @Test
  void shouldFindKeysForUsername() throws IOException {
    String raw = GPGTestHelper.readResourceAsString("single.asc");
    String raw2= GPGTestHelper.readResourceAsString("subkeys.asc");

    RawGpgKey key1 = new RawGpgKey("1", "1", "trillian", raw, Collections.emptySet(), Instant.now());
    RawGpgKey key2 = new RawGpgKey("2", "2", "trillian", raw2, Collections.emptySet(), Instant.now());
    when(publicKeyStore.findByUsername("trillian")).thenReturn(ImmutableList.of(key1, key2));

    Iterable<PublicKey> keys = gpg.findPublicKeysByUsername("trillian");

    assertThat(keys).hasSize(2);
    PublicKey key = keys.iterator().next();
    assertThat(key.getOwner()).isPresent();
    assertThat(key.getOwner().get()).contains("trillian");
  }

  @Test
  void shouldImportExportedGeneratedPrivateKey() throws NoSuchAlgorithmException, PGPException, IOException, InvalidAlgorithmParameterException {
    final PGPSecretKeyRing secretKeys = GPGKeyPairGenerator.generateKeyPair();
    final String exportedPrivateKey = PGPainless.asciiArmor(secretKeys);
    final PGPPrivateKey privateKey = KeysExtractor.extractPrivateKey(exportedPrivateKey);
    assertThat(privateKey).isNotNull();
  }

  @Test
  void shouldCreateSignature() throws IOException {
    SecurityUtils.setSecurityManager(new DefaultSecurityManager());
    Subject subjectUnderTest = MockUtil.createUserSubject(SecurityUtils.getSecurityManager());
    ThreadContext.bind(subjectUnderTest);

    String raw = GPGTestHelper.readResourceAsString("private-key.asc");
    final DefaultPrivateKey privateKey = DefaultPrivateKey.parseRaw(raw);
    final byte[] signature = privateKey.sign("This is a test commit".getBytes());
    final String signatureString = new String(signature);
    assertThat(signature).isNotEmpty();
    assertThat(signatureString)
      .startsWith("-----BEGIN PGP SIGNATURE-----")
      .contains("-----END PGP SIGNATURE-----");
  }

  @Test
  void shouldReturnGeneratedPrivateKeyIfNoneStored() {
    SecurityUtils.setSecurityManager(new DefaultSecurityManager());
    Subject subjectUnderTest = MockUtil.createUserSubject(SecurityUtils.getSecurityManager());
    ThreadContext.bind(subjectUnderTest);

    final PrivateKey privateKey = gpg.getPrivateKey();
    assertThat(privateKey).isNotNull();

    verify(privateKeyStore, atLeastOnce()).setForUserId(eq(subjectUnderTest.getPrincipal().toString()), anyString());
    verify(publicKeyStore, atLeastOnce()).add(eq("Default SCM-Manager Signing Key"), eq(subjectUnderTest.getPrincipal().toString()), anyString(), eq(true));
  }

  @Test
  void shouldReturnStoredPrivateKey() throws IOException {
    SecurityUtils.setSecurityManager(new DefaultSecurityManager());
    Subject subjectUnderTest = MockUtil.createUserSubject(SecurityUtils.getSecurityManager());
    ThreadContext.bind(subjectUnderTest);

    String raw = GPGTestHelper.readResourceAsString("private-key.asc");
    when(privateKeyStore.getForUserId(subjectUnderTest.getPrincipal().toString())).thenReturn(Optional.of(raw));

    final PrivateKey privateKey = gpg.getPrivateKey();
    assertThat(privateKey).isNotNull();
    verify(privateKeyStore, never()).setForUserId(eq(subjectUnderTest.getPrincipal().toString()), anyString());
    verify(publicKeyStore, never()).add(eq("Default SCM-Manager Signing Key"), eq(subjectUnderTest.getPrincipal().toString()), anyString(), eq(true));

  }
}
