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

import jakarta.inject.Inject;
import org.apache.shiro.SecurityUtils;
import org.bouncycastle.bcpg.ArmoredInputStream;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.pgpainless.PGPainless;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.security.GPG;
import sonia.scm.security.PrivateKey;
import sonia.scm.security.PublicKey;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
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

    return key.map(RawGpgKeyToDefaultPublicKeyMapper::map);
  }

  @Override
  public Iterable<PublicKey> findPublicKeysByUsername(String username) {
    List<RawGpgKey> keys = publicKeyStore.findByUsername(username);

    if (!keys.isEmpty()) {
      return keys
        .stream()
        .map(rawGpgKey -> new DefaultPublicKey(rawGpgKey.getId(), rawGpgKey.getOwner(), rawGpgKey.getRaw(), rawGpgKey.getContacts()))
        .collect(Collectors.toSet());
    }

    return Collections.emptySet();
  }

  @Override
  public PrivateKey getPrivateKey() {
    final String userId = SecurityUtils.getSubject().getPrincipal().toString();
    final Optional<String> privateRawKey = privateKeyStore.getForUserId(userId);

    if (privateRawKey.isEmpty()) {
      try {
        PGPSecretKeyRing secretKeys = GPGKeyPairGenerator.generateKeyPair();

        final String rawPublicKey = PGPainless.asciiArmor(PGPainless.extractCertificate(secretKeys));
        final String rawPrivateKey = PGPainless.asciiArmor(secretKeys);

        privateKeyStore.setForUserId(userId, rawPrivateKey);
        publicKeyStore.add("Default SCM-Manager Signing Key", userId, rawPublicKey, true);

        return DefaultPrivateKey.parseRaw(rawPrivateKey);
      } catch (PGPException | NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
        throw new GPGException("Private key could not be generated", e);
      }
    } else {
      return DefaultPrivateKey.parseRaw(privateRawKey.get());
    }
  }

}
