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

package sonia.scm.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.SCMContextProvider;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.cache.MapCacheManager;
import sonia.scm.config.ScmConfiguration;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReleaseVersionCheckerTest {

  private ReleaseFeedParser feedReader;
  private ScmConfiguration scmConfiguration;
  private SCMContextProvider contextProvider;
  private ReleaseVersionChecker checker;

  @BeforeEach
  void setup() {
    feedReader = mock(ReleaseFeedParser.class);
    scmConfiguration = mock(ScmConfiguration.class);
    contextProvider = mock(SCMContextProvider.class);
    CacheManager cacheManager = new MapCacheManager();

    checker = new ReleaseVersionChecker(feedReader, scmConfiguration, contextProvider, cacheManager);
  }

  @Test
  void shouldReturnEmptyOptional() {
    when(scmConfiguration.getReleaseFeedUrl()).thenReturn("releaseFeed");
    when(feedReader.findLatestRelease("releaseFeed")).thenReturn(Optional.empty());

    Optional<UpdateInfo> updateInfo = checker.checkForNewerVersion();

    assertThat(updateInfo).isNotPresent();
  }

  @Test
  void shouldReturnUpdateInfoFromCache() {
    String url = "releaseFeed";
    when(scmConfiguration.getReleaseFeedUrl()).thenReturn(url);

    UpdateInfo cachedUpdateInfo = new UpdateInfo("1.42.9", "download-link");
    Cache<String, Optional<UpdateInfo>> cache = new MapCacheManager().getCache("sonia.cache.updateInfo");
    cache.put(url, Optional.of(cachedUpdateInfo));
    checker.setCache(cache);

    Optional<UpdateInfo> updateInfo = checker.checkForNewerVersion();

    assertThat(updateInfo).isPresent();
    assertThat(updateInfo.get().getLatestVersion()).isEqualTo("1.42.9");
    assertThat(updateInfo.get().getLink()).isEqualTo("download-link");
  }

  @Test
  void shouldReturnUpdateInfo() {
    UpdateInfo updateInfo = new UpdateInfo("2.5.0", "download-link");
    when(scmConfiguration.getReleaseFeedUrl()).thenReturn("releaseFeed");
    when(feedReader.findLatestRelease("releaseFeed")).thenReturn(Optional.of(updateInfo));
    when(contextProvider.getVersion()).thenReturn("1.9.0");

    Optional<UpdateInfo> latestRelease = checker.checkForNewerVersion();

    assertThat(latestRelease).isPresent();
    assertThat(latestRelease.get().getLatestVersion()).isEqualTo("2.5.0");
    assertThat(latestRelease.get().getLink()).isEqualTo("download-link");
  }
}
