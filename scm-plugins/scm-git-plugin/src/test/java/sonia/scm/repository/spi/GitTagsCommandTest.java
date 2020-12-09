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

package sonia.scm.repository.spi;

import com.github.sdorra.shiro.SubjectAware;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.repository.SignatureStatus;
import sonia.scm.repository.Tag;
import sonia.scm.security.GPG;
import sonia.scm.security.PublicKey;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SubjectAware(configuration = "classpath:sonia/scm/configuration/shiro.ini", username = "admin", password = "secret")
@RunWith(MockitoJUnitRunner.class)
public class GitTagsCommandTest extends AbstractGitCommandTestBase {

  @Mock
  GPG gpg;

  @Mock
  PublicKey publicKey;

  @Test
  public void shouldGetDatesCorrectly() throws IOException {
    final GitContext gitContext = createContext();
    final GitTagsCommand tagsCommand = new GitTagsCommand(gitContext, gpg);
    final List<Tag> tags = tagsCommand.getTags();
    assertThat(tags).hasSize(3);

    Tag annotatedTag = tags.get(0);
    assertThat(annotatedTag.getName()).isEqualTo("1.0.0");
    assertThat(annotatedTag.getDate()).contains(1598348105000L); // Annotated - Take tag date
    assertThat(annotatedTag.getRevision()).isEqualTo("fcd0ef1831e4002ac43ea539f4094334c79ea9ec");

    Tag lightweightTag = tags.get(2);
    assertThat(lightweightTag.getName()).isEqualTo("test-tag");
    assertThat(lightweightTag.getDate()).contains(1339416344000L); // Lightweight - Take commit date
    assertThat(lightweightTag.getRevision()).isEqualTo("86a6645eceefe8b9a247db5eb16e3d89a7e6e6d1");
  }

  @Test
  public void shouldGetSignatures() throws IOException {
    when(gpg.findPublicKeyId(ArgumentMatchers.any())).thenReturn("2BA27721F113C005CC16F06BAE63EFBC49F140CF");
    when(gpg.findPublicKey("2BA27721F113C005CC16F06BAE63EFBC49F140CF")).thenReturn(Optional.of(publicKey));
    String signature = "-----BEGIN PGP SIGNATURE-----\n" +
      "\n" +
      "iQEzBAABCgAdFiEEK6J3IfETwAXMFvBrrmPvvEnxQM8FAl+9acoACgkQrmPvvEnx\n" +
      "QM9abwgAnGP+Y/Ijli+PAsimfOmZQWYepjptoOv9m7i3bnHv8V+Qg6cm51I3E0YV\n" +
      "R2QaxxzW9PgS4hcES+L1qs8Lwo18RurF469eZEmNb8DcUFJ3sEWeHlIl5wZNNo/v\n" +
      "jJm0d9LNcSmtAIiQ8eDMoGdFXJzHewGickLOSsQGmfZgZus4Qlsh7r3BZTI1Zwd/\n" +
      "6jaBFctX13FuepCTxq2SjEfRaQHIYkyFQq2o6mjL5S2qfYJ/S//gcCCzxllQrisF\n" +
      "5fRW3LzLI4eXFH0vua7+UzNS2Rwpifg2OENJA/Kn+3R36LWEGxFK9pNqjVPRAcQj\n" +
      "1vSkcjK26RqhAqCjNLSagM8ATZrh+g==\n" +
      "=kUKm\n" +
      "-----END PGP SIGNATURE-----\n";
    String signedContent = "object 592d797cd36432e591416e8b2b98154f4f163411\n" +
      "type commit\n" +
      "tag signedtag\n" +
      "tagger Arthur Dent <arthur.dent@hitchhiker.com> 1606248906 +0100\n" +
      "\n" +
      "this tag is signed\n";
    when(publicKey.verify(signedContent.getBytes(), signature.getBytes())).thenReturn(true);

    final GitContext gitContext = createContext();
    final GitTagsCommand tagsCommand = new GitTagsCommand(gitContext, gpg);
    final List<Tag> tags = tagsCommand.getTags();

    assertThat(tags).hasSize(3);

    Tag signedTag = tags.get(1);
    assertThat(signedTag.getSignatures()).isNotEmpty();
    assertThat(signedTag.getSignatures().get(0).getStatus()).isEqualTo(SignatureStatus.VERIFIED);
  }

  @Override
  protected String getZippedRepositoryResource() {
    return "sonia/scm/repository/spi/scm-git-spi-test-tags.zip";
  }
}
