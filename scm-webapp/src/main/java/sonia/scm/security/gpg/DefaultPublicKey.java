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

import jakarta.annotation.Nullable;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Person;
import sonia.scm.security.PublicKey;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;

public class DefaultPublicKey implements PublicKey {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultPublicKey.class);

  private final String id;
  private final String owner;
  private final String raw;
  private final Set<Person> contacts;

  public DefaultPublicKey(String id, @Nullable String owner, String raw, Set<Person> contacts) {
    this.id = id;
    this.owner = owner;
    this.raw = raw;
    this.contacts = contacts;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public Set<String> getSubkeys() {
    Keys keys = Keys.resolve(raw);
    return keys.getSubs();
  }

  @Override
  public Optional<String> getOwner() {
    if (owner == null) {
      return Optional.empty();
    }
    return Optional.of(owner);
  }

  @Override
  public String getRaw() {
    return raw;
  }

  @Override
  public Set<Person> getContacts() {
    return contacts;
  }

  @Override
  public boolean verify(InputStream stream, byte[] signature) {
    boolean verified = false;
    try {
      verified = verify(stream, asDecodedStream(signature));
    } catch (IOException | PGPException e) {
      LOG.error("Could not verify GPG key", e);
    }

    return verified;
  }

  private boolean verify(InputStream stream, InputStream signature) throws IOException, PGPException {
    PGPObjectFactory pgpObjectFactory = new JcaPGPObjectFactory(signature);
    Object o = pgpObjectFactory.nextObject();
    if (o instanceof PGPSignatureList) {
      return verify(stream, ((PGPSignatureList) o).get(0));
    } else if (o instanceof PGPCompressedData) {
      return verify(stream, ((PGPCompressedData) o).getDataStream());
    } else {
      LOG.warn("could not find valid signature, only found {}", o);
      return false;
    }
  }

  private boolean verify(InputStream stream, PGPSignature signature) throws IOException, PGPException {
    PGPPublicKey publicKey = findKey(signature);
    if (publicKey != null) {
      JcaPGPContentVerifierBuilderProvider provider = new JcaPGPContentVerifierBuilderProvider();
      signature.init(provider, publicKey);

      int bytesRead;
      byte[] buffer = new byte[1024];
      while ((bytesRead = stream.read(buffer, 0, buffer.length)) != -1) {
        signature.update(buffer, 0, bytesRead);
      }

      return signature.verify();
    } else {
      LOG.warn("failed to parse public gpg key");
    }
    return false;
  }

  private PGPPublicKey findKey(PGPSignature signature) throws IOException {
    PGPObjectFactory pgpObjectFactory = new JcaPGPObjectFactory(asDecodedStream(raw));
    PGPPublicKeyRing keyRing = (PGPPublicKeyRing) pgpObjectFactory.nextObject();
    return keyRing.getPublicKey(signature.getKeyID());
  }

  private InputStream asDecodedStream(String content) throws IOException {
    return asDecodedStream(content.getBytes(StandardCharsets.US_ASCII));
  }

  private InputStream asDecodedStream(byte[] bytes) throws IOException {
    return PGPUtil.getDecoderStream(new ByteArrayInputStream(bytes));
  }
}


