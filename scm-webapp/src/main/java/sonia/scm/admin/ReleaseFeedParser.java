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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.net.ahc.AdvancedHttpClient;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Singleton
public class ReleaseFeedParser {

  public static final int DEFAULT_TIMEOUT_IN_MILLIS = 1000;

  private static final Logger LOG = LoggerFactory.getLogger(ReleaseFeedParser.class);

  private final AdvancedHttpClient client;
  private final ExecutorService executorService;
  private final long timeoutInMillis;
  private Future<Optional<ReleaseInfo>> releaseInfoFuture;

  @Inject
  public ReleaseFeedParser(AdvancedHttpClient client) {
    this(client, DEFAULT_TIMEOUT_IN_MILLIS);
  }

  public ReleaseFeedParser(AdvancedHttpClient client, long timeoutInMillis) {
    this.client = client;
    this.timeoutInMillis = timeoutInMillis;
    this.executorService = Executors.newSingleThreadExecutor();
  }

  Optional<ReleaseInfo> findLatestRelease(String url) {
    Future<Optional<ReleaseInfo>> currentReleaseInfoFuture;
    boolean releaseInfoFutureCreated = false;
    try {
      synchronized (this) {
        currentReleaseInfoFuture = this.releaseInfoFuture;
        if (currentReleaseInfoFuture == null) {
          currentReleaseInfoFuture = submitRequest(url);
          this.releaseInfoFuture = currentReleaseInfoFuture;
          releaseInfoFutureCreated = true;
        }
      }
      try {
        return currentReleaseInfoFuture.get(timeoutInMillis, TimeUnit.MILLISECONDS);
      } catch (Exception e) {
        LOG.error("Could not read release feed", e);
        return Optional.empty();
      }
    } finally {
      if (releaseInfoFutureCreated) {
        synchronized (this) {
          this.releaseInfoFuture = null;
        }
      }
    }
  }

  private Future<Optional<ReleaseInfo>> submitRequest(String url) {
    return executorService.submit(() -> {
      LOG.info("Search for newer versions of SCM-Manager");
      Optional<ReleaseFeedDto.Release> latestRelease = parseLatestReleaseFromRssFeed(url);
      return latestRelease.map(release -> new ReleaseInfo(release.getTitle(), release.getLink()));
    });
  }

  private Optional<ReleaseFeedDto.Release> parseLatestReleaseFromRssFeed(String url) {
    try {
      ReleaseFeedDto releaseFeed = client.get(url).request().contentFromXml(ReleaseFeedDto.class);
      return filterForLatestRelease(releaseFeed);
    } catch (IOException e) {
      LOG.error("Could not parse release feed from {}", url, e);
      return Optional.empty();
    }
  }

  private Optional<ReleaseFeedDto.Release> filterForLatestRelease(ReleaseFeedDto releaseFeed) {
    return releaseFeed.getChannel().getReleases()
      .stream()
      .max(Comparator.comparing(ReleaseFeedDto.Release::getPubDate));
  }
}
