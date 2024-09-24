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
