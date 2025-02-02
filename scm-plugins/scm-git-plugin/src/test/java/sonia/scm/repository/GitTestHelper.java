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

package sonia.scm.repository;

import org.eclipse.jgit.lib.GpgConfig;
import org.eclipse.jgit.lib.GpgSignature;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.Signer;
import org.eclipse.jgit.transport.CredentialsProvider;
import sonia.scm.security.GPG;
import sonia.scm.security.PrivateKey;
import sonia.scm.security.PublicKey;

import java.util.Collections;
import java.util.Optional;

public final class GitTestHelper {

  private GitTestHelper() {
  }

  public static GitChangesetConverterFactory createConverterFactory() {
    return new GitChangesetConverterFactory(new NoopGPG());
  }

  public static class SimpleGpgSigner implements Signer {

    public static byte[] getSignature() {
      return "SIGNATURE".getBytes();
    }

    @Override
    public GpgSignature sign(Repository repository, GpgConfig gpgConfig, byte[] bytes, PersonIdent personIdent, String s, CredentialsProvider credentialsProvider) {
      return new GpgSignature(SimpleGpgSigner.getSignature());
    }

    @Override
    public boolean canLocateSigningKey(Repository repository, GpgConfig gpgConfig, PersonIdent personIdent, String s, CredentialsProvider credentialsProvider) {
      return true;
    }

  }

  private static class NoopGPG implements GPG {

    @Override
    public String findPublicKeyId(byte[] signature) {
      return "secret-key";
    }

    @Override
    public Optional<PublicKey> findPublicKey(String id) {
      return Optional.empty();
    }

    @Override
    public Iterable<PublicKey> findPublicKeysByUsername(String username) {
      return Collections.emptySet();
    }

    @Override
    public PrivateKey getPrivateKey() {
      return null;
    }
  }
}
