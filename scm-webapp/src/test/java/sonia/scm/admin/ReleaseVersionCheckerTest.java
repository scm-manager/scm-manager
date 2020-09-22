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
import java.time.Instant;
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
  void shouldReturnEmptyOptional() throws IOException {
    when(scmConfiguration.getReleaseFeedUrl()).thenReturn("releaseFeed");
    when(feedReader.findLatestRelease("releaseFeed")).thenReturn(Optional.empty());

    Optional<ReleaseInfo> releaseInfo = checker.checkForNewerVersion();

    assertThat(releaseInfo).isNotPresent();
  }

  @Test
  void shouldReturnReleaseInfoFromCache() {
    ReleaseInfo cachedReleaseInfo = new ReleaseInfo("1.42.9", "download-link", Instant.now());
    Cache<String, ReleaseInfo> cache = new MapCacheManager().getCache("sonia.cache.releaseInfo");
    cache.put("latest", cachedReleaseInfo);
    checker.setCache(cache);

    Optional<ReleaseInfo> releaseInfo = checker.checkForNewerVersion();

    assertThat(releaseInfo).isPresent();
    assertThat(releaseInfo.get().getTitle()).isEqualTo("1.42.9");
    assertThat(releaseInfo.get().getLink()).isEqualTo("download-link");
  }

  @Test
  void shouldReturnReleaseInfo() throws IOException {
    ReleaseInfo releaseInfo = new ReleaseInfo("2.0.0", "download-link", Instant.now());
    when(scmConfiguration.getReleaseFeedUrl()).thenReturn("releaseFeed");
    when(feedReader.findLatestRelease("releaseFeed")).thenReturn(Optional.of(releaseInfo));
    when(contextProvider.getVersion()).thenReturn("1.9.0");

    Optional<ReleaseInfo> latestRelease = checker.checkForNewerVersion();

    assertThat(latestRelease).isPresent();
    assertThat(latestRelease.get().getTitle()).isEqualTo("2.0.0");
    assertThat(latestRelease.get().getLink()).isEqualTo("download-link");
  }
}
