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

package sonia.scm.web.lfs;

import org.eclipse.jgit.lfs.lib.LongObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.security.AccessToken;
import sonia.scm.store.BlobStore;

import java.util.Date;

import static java.time.Instant.parse;
import static java.util.Date.from;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.jgit.lfs.lib.LongObjectId.fromString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScmBlobLfsRepositoryTest {

  static final Repository REPOSITORY = new Repository("1", "git", "space", "X");
  static final Date EXPIRATION = from(parse("2007-05-03T10:15:30.00Z"));
  static final LongObjectId OBJECT_ID = fromString("976ed944c37cc5d1606af316937edb9d286ecf6c606af316937edb9d286ecf6c");

  @Mock
  BlobStore blobStore;
  @Mock
  LfsAccessTokenFactory tokenFactory;

  ScmBlobLfsRepository lfsRepository;

  @BeforeEach
  void initializeLfsRepository() {
    lfsRepository = new ScmBlobLfsRepository(REPOSITORY, blobStore, tokenFactory, "http://scm.org/");
  }

  @BeforeEach
  void initAuthorizationToken() {
    AccessToken readToken = createToken("READ_TOKEN");
    lenient().when(this.tokenFactory.createReadAccessToken(REPOSITORY))
      .thenReturn(readToken);
    AccessToken writeToken = createToken("WRITE_TOKEN");
    lenient().when(this.tokenFactory.createWriteAccessToken(REPOSITORY))
      .thenReturn(writeToken);
  }

  AccessToken createToken(String mockedValue) {
    AccessToken accessToken = mock(AccessToken.class);
    lenient().when(accessToken.getExpiration()).thenReturn(EXPIRATION);
    lenient().when(accessToken.compact()).thenReturn(mockedValue);
    return accessToken;
  }

  @Test
  void shouldTakeExpirationFromToken() {
    ExpiringAction downloadAction = lfsRepository.getDownloadAction(OBJECT_ID);
    assertThat(downloadAction.expires_at).isEqualTo("2007-05-03T10:15:30Z");
  }

  @Test
  void shouldContainReadTokenForDownlo() {
    ExpiringAction downloadAction = lfsRepository.getDownloadAction(OBJECT_ID);
    assertThat(downloadAction.header.get("Authorization")).isEqualTo("Bearer READ_TOKEN");
  }

  @Test
  void shouldContainWriteTokenForUpload() {
    ExpiringAction downloadAction = lfsRepository.getUploadAction(OBJECT_ID, 42L);
    assertThat(downloadAction.header.get("Authorization")).isEqualTo("Bearer WRITE_TOKEN");
  }

  @Test
  void shouldContainUrl() {
    ExpiringAction downloadAction = lfsRepository.getDownloadAction(OBJECT_ID);
    assertThat(downloadAction.href).isEqualTo("http://scm.org/976ed944c37cc5d1606af316937edb9d286ecf6c606af316937edb9d286ecf6c");
  }

  @Test
  void shouldCreateTokenForDownloadActionOnlyOnce() {
    lfsRepository.getDownloadAction(OBJECT_ID);
    lfsRepository.getDownloadAction(OBJECT_ID);
    verify(tokenFactory, times(1)).createReadAccessToken(REPOSITORY);
  }

  @Test
  void shouldCreateTokenForUploadActionOnlyOnce() {
    lfsRepository.getUploadAction(OBJECT_ID, 42L);
    lfsRepository.getUploadAction(OBJECT_ID, 42L);
    verify(tokenFactory, times(1)).createWriteAccessToken(REPOSITORY);
  }
}

