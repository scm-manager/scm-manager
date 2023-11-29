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
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContextProvider;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.version.Version;

import java.util.Optional;

public class ReleaseVersionChecker {

  private static final Logger LOG = LoggerFactory.getLogger(ReleaseVersionChecker.class);
  private static final String CACHE_NAME = "sonia.cache.updateInfo";

  private final ReleaseFeedParser releaseFeedParser;
  private final ScmConfiguration scmConfiguration;
  private final SCMContextProvider scmContextProvider;
  private Cache<String, Optional<UpdateInfo>> cache;

  @Inject
  public ReleaseVersionChecker(ReleaseFeedParser releaseFeedParser, ScmConfiguration scmConfiguration, SCMContextProvider scmContextProvider, CacheManager cacheManager) {
    this.releaseFeedParser = releaseFeedParser;
    this.scmConfiguration = scmConfiguration;
    this.scmContextProvider = scmContextProvider;
    this.cache = cacheManager.getCache(CACHE_NAME);
  }

  @VisibleForTesting
  void setCache(Cache<String, Optional<UpdateInfo>> cache) {
    this.cache = cache;
  }

  public Optional<UpdateInfo> checkForNewerVersion() {
    if (cache.contains(scmConfiguration.getReleaseFeedUrl())) {
      return cache.get(scmConfiguration.getReleaseFeedUrl());
    }
    return findLatestRelease();
  }

  private Optional<UpdateInfo> findLatestRelease() {
    String releaseFeedUrl = scmConfiguration.getReleaseFeedUrl();
    Optional<UpdateInfo> latestRelease = releaseFeedParser.findLatestRelease(releaseFeedUrl);
    if (latestRelease.isPresent() && isNewerVersion(latestRelease.get())) {
      cache.put(scmConfiguration.getReleaseFeedUrl(), latestRelease);
      return latestRelease;
    }
    // we cache that no new version was available to prevent request every time
    LOG.debug("No newer version found for SCM-Manager");
    cache.put(scmConfiguration.getReleaseFeedUrl(), Optional.empty());
    return Optional.empty();
  }

  private boolean isNewerVersion(UpdateInfo updateInfo) {
    Version versionFromReleaseFeed = Version.parse(updateInfo.getLatestVersion());
    return versionFromReleaseFeed.isNewer(scmContextProvider.getVersion());
  }
}
