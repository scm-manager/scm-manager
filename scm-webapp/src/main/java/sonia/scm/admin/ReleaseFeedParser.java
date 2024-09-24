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
import com.google.common.base.Strings;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.metrics.Metrics;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.version.Version;

import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Singleton
public class ReleaseFeedParser {

  public static final int DEFAULT_TIMEOUT_IN_MILLIS = 5000;

  private static final Logger LOG = LoggerFactory.getLogger(ReleaseFeedParser.class);

  @VisibleForTesting
  static final String SPAN_KIND = "Release Feed";

  private final AdvancedHttpClient client;
  private final ExecutorService executorService;
  private final long timeoutInMillis;
  private Future<Optional<UpdateInfo>> updateInfoFuture;

  @Inject
  public ReleaseFeedParser(AdvancedHttpClient client, MeterRegistry registry) {
    this(client, registry, DEFAULT_TIMEOUT_IN_MILLIS);
  }

  public ReleaseFeedParser(AdvancedHttpClient client, MeterRegistry registry, long timeoutInMillis) {
    this.client = client;
    this.timeoutInMillis = timeoutInMillis;
    this.executorService = createExecutorService(registry);
  }

  private ExecutorService createExecutorService(MeterRegistry registry) {
    ExecutorService executor = Executors.newSingleThreadExecutor(
      new ThreadFactoryBuilder()
        .setNameFormat("ReleaseFeedParser")
        .build()
    );
    Metrics.executor(registry, executor, "ReleaseFeedParser", "single");
    return executor;
  }

  Optional<UpdateInfo> findLatestRelease(String url) {
    Future<Optional<UpdateInfo>> currentUpdateInfoFuture;
    boolean updateInfoFutureCreated = false;
    try {
      synchronized (this) {
        currentUpdateInfoFuture = this.updateInfoFuture;
        if (currentUpdateInfoFuture == null) {
          currentUpdateInfoFuture = submitRequest(url);
          this.updateInfoFuture = currentUpdateInfoFuture;
          updateInfoFutureCreated = true;
        }
      }
      try {
        return currentUpdateInfoFuture.get(timeoutInMillis, TimeUnit.MILLISECONDS);
      } catch (Exception e) {
        LOG.error("Could not read release feed", e);
        return Optional.empty();
      }
    } finally {
      if (updateInfoFutureCreated) {
        synchronized (this) {
          this.updateInfoFuture = null;
        }
      }
    }
  }

  private Future<Optional<UpdateInfo>> submitRequest(String url) {
    return executorService.submit(() -> {
      LOG.info("Search for newer versions of SCM-Manager");
      Optional<ReleaseFeedDto.Release> latestRelease = parseLatestReleaseFromRssFeed(url);
      return latestRelease.map(release -> new UpdateInfo(release.getTitle(), release.getLink()));
    });
  }

  private Optional<ReleaseFeedDto.Release> parseLatestReleaseFromRssFeed(String url) {
    try {
      if (Strings.isNullOrEmpty(url)) {
        return Optional.empty();
      }
      ReleaseFeedDto releaseFeed = client.get(url)
        .spanKind(SPAN_KIND)
        .request()
        .contentFromXml(ReleaseFeedDto.class);
      return filterForLatestRelease(releaseFeed);
    } catch (Exception e) {
      LOG.error("Could not parse release feed from {}", url, e);
      return Optional.empty();
    }
  }

  private Optional<ReleaseFeedDto.Release> filterForLatestRelease(ReleaseFeedDto releaseFeed) {
    return releaseFeed.getChannel().getReleases()
      .stream()
      .min(Comparator.comparing(release -> Version.parse(release.getTitle().trim())));
  }
}
