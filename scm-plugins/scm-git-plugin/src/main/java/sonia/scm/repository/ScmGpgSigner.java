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
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.GpgSignature;
import org.eclipse.jgit.lib.GpgSigner;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.transport.CredentialsProvider;
import sonia.scm.security.GPG;

import java.io.UnsupportedEncodingException;

public class ScmGpgSigner extends GpgSigner {

  private final GPG gpg;

  @Inject
  public ScmGpgSigner(GPG gpg) {
    this.gpg = gpg;
  }

  @Override
  public void sign(CommitBuilder commitBuilder, String keyId, PersonIdent personIdent, CredentialsProvider credentialsProvider) throws CanceledException {
    try {
      final byte[] signature = this.gpg.getPrivateKey().sign(commitBuilder.build());
      commitBuilder.setGpgSignature(new GpgSignature(signature));
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public boolean canLocateSigningKey(String keyId, PersonIdent personIdent, CredentialsProvider credentialsProvider) throws CanceledException {
    return true;
  }
}
