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

import jakarta.inject.Inject;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.UnsupportedSigningFormatException;
import org.eclipse.jgit.lib.GpgConfig;
import org.eclipse.jgit.lib.GpgSignature;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.Signer;
import org.eclipse.jgit.transport.CredentialsProvider;
import sonia.scm.security.GPG;

import java.io.IOException;

public class ScmGpgSigner implements Signer {

  private final GPG gpg;

  @Inject
  public ScmGpgSigner(GPG gpg) {
    this.gpg = gpg;
  }

  @Override
  public GpgSignature sign(Repository repository, GpgConfig gpgConfig, byte[] bytes, PersonIdent personIdent, String s, CredentialsProvider credentialsProvider) throws CanceledException, IOException, UnsupportedSigningFormatException {
    final byte[] signature = this.gpg.getPrivateKey().sign(bytes);
    return new GpgSignature(signature);
  }

  @Override
  public boolean canLocateSigningKey(Repository repository, GpgConfig gpgConfig, PersonIdent personIdent, String s, CredentialsProvider credentialsProvider) throws CanceledException {
    return true;
  }
}
