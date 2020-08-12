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

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.internal.storage.dfs.DfsRepositoryDescription;
import org.eclipse.jgit.internal.storage.dfs.InMemoryRepository;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.GpgSigner;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.security.GPG;
import sonia.scm.security.PrivateKey;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScmGpgSignerTest {

  @Mock
  GPG gpg;

  @Mock
  PersonIdent personIdent;

  @Mock
  CredentialsProvider credentialsProvider;

  private ScmGpgSigner signer;

  private final PrivateKey privateKey = new PrivateKey() {
    @Override
    public String getId() {
      return "Private Key";
    }

    @Override
    public byte[] sign(InputStream stream) {
      return "MY FANCY SIGNATURE".getBytes();
    }
  };

  @BeforeEach
  void beforeEach() {
    signer = new ScmGpgSigner(gpg);
  }

  @Test
  void sign(@TempDir Path workdir) throws Exception {

    when(gpg.getPrivateKey()).thenReturn(privateKey);

    GpgSigner.setDefault(signer);

    Path repositoryPath = workdir.resolve("repository");
    Git git = Git.init().setDirectory(repositoryPath.toFile()).call();

    Files.write(repositoryPath.resolve("README.md"), "# Hello".getBytes(StandardCharsets.UTF_8));
    git.add().addFilepattern("README.md").call();

    git.commit()
      .setAuthor("Bob The Signer", "sign@bob.de")
      .setMessage("Signed from Bob")
      .setSign(true)
      .setSigningKey("Private Key")
      .call();

    RevCommit commit = git.log().setMaxCount(1).call().iterator().next();

    final byte[] rawCommit = commit.getRawBuffer();
    final String commitString = new String(rawCommit);
    assertThat(commitString).contains("gpgsig MY FANCY SIGNATURE");
  }

  @Test
  void canLocateSigningKey() throws CanceledException {
    assertThat(signer.canLocateSigningKey("foo", personIdent, credentialsProvider)).isTrue();
  }
}
