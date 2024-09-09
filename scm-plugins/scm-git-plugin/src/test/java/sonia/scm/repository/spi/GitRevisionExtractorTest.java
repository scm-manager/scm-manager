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

import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GitRevisionExtractorTest {

  @Test
  void shouldReturnRevisionFromRevCommit() {
    RevCommit revCommit = mock(RevCommit.class);
    when(revCommit.toString()).thenReturn("commit 123456abcdef -t 4561");
    String revision = GitRevisionExtractor.extractRevisionFromRevCommit(revCommit);
    assertThat(revision).isEqualTo("123456abcdef");
  }
}
