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

import org.apache.commons.io.IOUtils;
import org.apache.shiro.SecurityUtils;
import org.bouncycastle.bcpg.ArmoredInputStream;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.bcpg.PublicKeyAlgorithmTags;
import org.bouncycastle.bcpg.SymmetricKeyAlgorithmTags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPKeyPair;
import org.bouncycastle.openpgp.PGPKeyRing;
import org.bouncycastle.openpgp.PGPKeyRingGenerator;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPDigestCalculatorProviderBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPKeyPair;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyEncryptorBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Person;
import sonia.scm.security.GPG;
import sonia.scm.security.PrivateKey;
import sonia.scm.security.PublicKey;
import sonia.scm.user.User;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DefaultGPG implements GPG {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultGPG.class);

  private final PublicKeyStore publicKeyStore;
  private final PrivateKeyStore privateKeyStore;

  @Inject
  public DefaultGPG(PublicKeyStore publicKeyStore, PrivateKeyStore privateKeyStore) {
    this.publicKeyStore = publicKeyStore;
    this.privateKeyStore = privateKeyStore;
  }

  @Override
  public String findPublicKeyId(byte[] signature) {
    try {
      ArmoredInputStream armoredInputStream = new ArmoredInputStream(new ByteArrayInputStream(signature));
      PGPObjectFactory pgpObjectFactory = new PGPObjectFactory(armoredInputStream, new JcaKeyFingerprintCalculator());
      PGPSignatureList signatures = (PGPSignatureList) pgpObjectFactory.nextObject();
      return "0x" + Long.toHexString(signatures.get(0).getKeyID()).toUpperCase();
    } catch (IOException e) {
      LOG.error("Could not find public key id in signature");
    }
    return "";
  }

  @Override
  public Optional<PublicKey> findPublicKey(String id) {
    Optional<RawGpgKey> key = publicKeyStore.findById(id);

    return key.map(rawGpgKey -> new GpgKey(rawGpgKey.getId(), rawGpgKey.getOwner(), rawGpgKey.getRaw(), rawGpgKey.getContacts()));
  }

  @Override
  public Iterable<PublicKey> findPublicKeysByUsername(String username) {
    List<RawGpgKey> keys = publicKeyStore.findByUsername(username);

    if (!keys.isEmpty()) {
      return keys
        .stream()
        .map(rawGpgKey -> new GpgKey(rawGpgKey.getId(), rawGpgKey.getOwner(), rawGpgKey.getRaw(), rawGpgKey.getContacts()))
        .collect(Collectors.toSet());
    }

    return Collections.emptySet();
  }

  @Override
  public PrivateKey getPrivateKey() {
    final String userId = SecurityUtils.getSubject().getPrincipal().toString();
    final Optional<String> privateRawKey = privateKeyStore.getForUserId(userId);

    if (!privateRawKey.isPresent()) {
      try {
        final PGPKeyRingGenerator keyPair = generateKeyPair();

        final String rawPublicKey = exportKeyRing(keyPair.generatePublicKeyRing());
        final String rawPrivateKey = exportKeyRing(keyPair.generateSecretKeyRing());

        privateKeyStore.setForUserId(userId, rawPrivateKey);
        publicKeyStore.add("Default SCM-Manager Signing Key", userId, rawPublicKey, true);

        return new DefaultPrivateKey(rawPrivateKey);
      } catch (PGPException | NoSuchAlgorithmException | NoSuchProviderException | IOException e) {
        throw new IllegalStateException("Private key could not be generated", e);
      }
    } else {
      return new DefaultPrivateKey(privateRawKey.get());
    }
  }

  String exportKeyRing(PGPKeyRing keyRing) throws IOException {
    final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    final ArmoredOutputStream armoredOutputStream = new ArmoredOutputStream(byteArrayOutputStream);
    keyRing.encode(armoredOutputStream);
    armoredOutputStream.close();
    return new String(byteArrayOutputStream.toByteArray());
  }

  PGPKeyRingGenerator generateKeyPair() throws PGPException, NoSuchProviderException, NoSuchAlgorithmException {
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
    keyPairGenerator.initialize(2048);

    KeyPair pair = keyPairGenerator.generateKeyPair();

    PGPKeyPair keyPair = new JcaPGPKeyPair(PublicKeyAlgorithmTags.RSA_GENERAL, pair, new Date());
    final User user = SecurityUtils.getSubject().getPrincipals().oneByType(User.class);
    final Person person = new Person(user.getDisplayName(), user.getMail());

    return new PGPKeyRingGenerator(
      PGPSignature.POSITIVE_CERTIFICATION,
      keyPair,
      person.toString(),
      new JcaPGPDigestCalculatorProviderBuilder().build().get(HashAlgorithmTags.SHA1),
      null,
      null,
      new JcaPGPContentSignerBuilder(PublicKeyAlgorithmTags.RSA_GENERAL, HashAlgorithmTags.SHA1),
      new JcePBESecretKeyEncryptorBuilder(SymmetricKeyAlgorithmTags.AES_256).build(new char[]{})
    );
  }

  static class DefaultPrivateKey implements PrivateKey {

    final Optional<PGPPrivateKey> privateKey;

    DefaultPrivateKey(String rawPrivateKey) {
      privateKey = PgpPrivateKeyExtractor.getFromRawKey(rawPrivateKey);
    }

    @Override
    public String getId() {
      if (privateKey.isPresent()) {
        return Keys.createId(privateKey.get());
      } else {
        return null;
      }
    }

    @Override
    public byte[] sign(InputStream stream) {

      PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator(
        new JcaPGPContentSignerBuilder(
          PublicKeyAlgorithmTags.RSA_GENERAL,
          HashAlgorithmTags.SHA1).setProvider(BouncyCastleProvider.PROVIDER_NAME)
      );

      if (privateKey.isPresent()) {
        try {
          signatureGenerator.init(PGPSignature.BINARY_DOCUMENT, privateKey.get());
        } catch (PGPException e) {
          throw new IllegalStateException("Could not initialize signature generator", e);
        }
      } else {
        throw new IllegalStateException("Missing private key");
      }

      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      try (BCPGOutputStream out = new BCPGOutputStream(new ArmoredOutputStream(buffer))) {
        signatureGenerator.update(IOUtils.toByteArray(stream));
        signatureGenerator.generate().encode(out);
      } catch (PGPException | IOException e) {
        throw new IllegalStateException("Could not create signature", e);
      }

      return buffer.toByteArray();
    }
  }
}
