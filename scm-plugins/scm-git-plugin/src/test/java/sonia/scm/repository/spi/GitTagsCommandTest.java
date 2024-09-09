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
    final GitTagsCommand tagsCommand = new GitTagsCommand(gitContext, new GitTagConverter(gpg));
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
  public void shouldGetTagsForRevision() throws IOException {
    GitContext gitContext = createContext();
    GitTagsCommand tagsCommand = new GitTagsCommand(gitContext, new GitTagConverter(gpg));

    List<Tag> tags = tagsCommand.getTags("86a6645eceefe8b9a247db5eb16e3d89a7e6e6d1");

    assertThat(tags).hasSize(2);
  }

  @Test
  public void shouldGetTagsForShortenedRevision() throws IOException {
    GitContext gitContext = createContext();
    GitTagsCommand tagsCommand = new GitTagsCommand(gitContext, new GitTagConverter(gpg));

    List<Tag> tags = tagsCommand.getTags("86a6645");

    assertThat(tags).hasSize(2);
  }

  @Test
  public void shouldGetSignatures() throws IOException {
    when(gpg.findPublicKeyId(ArgumentMatchers.any())).thenReturn("2BA27721F113C005CC16F06BAE63EFBC49F140CF");
    when(gpg.findPublicKey("2BA27721F113C005CC16F06BAE63EFBC49F140CF")).thenReturn(Optional.of(publicKey));
    String signature = """
            -----BEGIN PGP SIGNATURE-----

            iQEzBAABCgAdFiEEK6J3IfETwAXMFvBrrmPvvEnxQM8FAl+9acoACgkQrmPvvEnx
            QM9abwgAnGP+Y/Ijli+PAsimfOmZQWYepjptoOv9m7i3bnHv8V+Qg6cm51I3E0YV
            R2QaxxzW9PgS4hcES+L1qs8Lwo18RurF469eZEmNb8DcUFJ3sEWeHlIl5wZNNo/v
            jJm0d9LNcSmtAIiQ8eDMoGdFXJzHewGickLOSsQGmfZgZus4Qlsh7r3BZTI1Zwd/
            6jaBFctX13FuepCTxq2SjEfRaQHIYkyFQq2o6mjL5S2qfYJ/S//gcCCzxllQrisF
            5fRW3LzLI4eXFH0vua7+UzNS2Rwpifg2OENJA/Kn+3R36LWEGxFK9pNqjVPRAcQj
            1vSkcjK26RqhAqCjNLSagM8ATZrh+g==
            =kUKm
            -----END PGP SIGNATURE-----
            """;
    String signedContent = """
            object 592d797cd36432e591416e8b2b98154f4f163411
            type commit
            tag signedtag
            tagger Arthur Dent <arthur.dent@hitchhiker.com> 1606248906 +0100

            this tag is signed
            """;
    when(publicKey.verify(signedContent.getBytes(), signature.getBytes())).thenReturn(true);

    final GitContext gitContext = createContext();
    final GitTagsCommand tagsCommand = new GitTagsCommand(gitContext, new GitTagConverter(gpg));
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
