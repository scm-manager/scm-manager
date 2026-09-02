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

package sonia.scm.util;

import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScmSecureTarArchiveInputStreamTest {

  @Test
  public void shouldSuccessfullyIterateOverEveryArchiveEntries() throws IOException {
    InputStream genericStream = Resources.getResource("sonia/scm/util/valid-archive.tar").openStream();
    ScmSecureTarArchiveInputStream tarArchiveStream = new ScmSecureTarArchiveInputStream(genericStream);

    assertThat(tarArchiveStream.getNextEntry().getName()).isEqualTo("valid-file.md");
    assertThat(tarArchiveStream.getNextEntry().getName()).isEqualTo("valid-folder/");
    assertThat(tarArchiveStream.getNextEntry().getName()).isEqualTo("valid-folder/another-valid-file.md");
    assertThat(tarArchiveStream.getNextEntry().getName()).isEqualTo("valid-subarchive.tar");
    assertThat(tarArchiveStream.getNextEntry()).isNull();
  }

  @Test
  public void shouldThrowExceptionBecauseOfEntryWithPathTraversal() throws IOException {
    InputStream genericStream = Resources.getResource("sonia/scm/util/path-traversal.tar").openStream();
    ScmSecureTarArchiveInputStream tarArchiveStream = new ScmSecureTarArchiveInputStream(genericStream);

    assertThat(tarArchiveStream.getNextEntry().getName()).isEqualTo("legal-file.md");
    assertThatThrownBy(tarArchiveStream::getNextEntry).isInstanceOf(InsecureTarArchiveEntryException.class);
  }
}
