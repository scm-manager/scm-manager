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
