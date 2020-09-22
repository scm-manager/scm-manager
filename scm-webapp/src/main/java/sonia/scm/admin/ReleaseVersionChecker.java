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

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContextProvider;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.version.Version;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Optional;

public class ReleaseVersionChecker {

  private static final Logger LOG = LoggerFactory.getLogger(ReleaseVersionChecker.class);
  private static final String CACHE_NAME = "sonia.cache.releaseInfo";

  private final ReleaseFeedParser releaseFeedParser;
  private final ScmConfiguration scmConfiguration;
  private final SCMContextProvider scmContextProvider;
  private Cache<String, ReleaseInfo> cache;

  @Inject
  public ReleaseVersionChecker(ReleaseFeedParser releaseFeedParser, ScmConfiguration scmConfiguration, SCMContextProvider scmContextProvider, CacheManager cacheManager) {
    this.releaseFeedParser = releaseFeedParser;
    this.scmConfiguration = scmConfiguration;
    this.scmContextProvider = scmContextProvider;
    this.cache = cacheManager.getCache(CACHE_NAME);
  }

  @VisibleForTesting
  void setCache(Cache<String, ReleaseInfo> cache) {
    this.cache = cache;
  }

  public Optional<ReleaseInfo> checkForNewerVersion() {
    ReleaseInfo cachedReleaseInfo = cache.get("latest");
    if (cachedReleaseInfo != null) {
      return Optional.of(cachedReleaseInfo);
    } else {
      return findLatestReleaseInRssFeed();
    }
  }

  private Optional<ReleaseInfo> findLatestReleaseInRssFeed() {
    try {
      String releaseFeedUrl = scmConfiguration.getReleaseFeedUrl();
      Optional<ReleaseInfo> latestRelease = releaseFeedParser.findLatestRelease(releaseFeedUrl);
      if (latestRelease.isPresent() && isNewerVersion(latestRelease.get())) {
        cache.put("latest", latestRelease.get());
        return latestRelease;
      }
    } catch (IOException e) {
      // This is an silent action. We don't want the user to get any kind of error for this.
      LOG.info("No newer version found for SCM-Manager");
      return Optional.empty();
    }
    return Optional.empty();
  }

  private boolean isNewerVersion(ReleaseInfo releaseInfo) {
    Version versionFromReleaseFeed = Version.parse(releaseInfo.getTitle());
    return versionFromReleaseFeed.isNewer(scmContextProvider.getVersion());
  }
}
