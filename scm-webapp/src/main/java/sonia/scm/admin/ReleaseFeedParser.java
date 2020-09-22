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
import java.io.IOException;
import java.util.Comparator;
import java.util.Optional;

public class ReleaseFeedParser {

  private static final Logger LOG = LoggerFactory.getLogger(ReleaseFeedParser.class);

  private final AdvancedHttpClient client;

  @Inject
  public ReleaseFeedParser(AdvancedHttpClient client) {
    this.client = client;
  }

  Optional<ReleaseInfo> findLatestRelease(String url) {
    LOG.info("Search for newer versions of SCM-Manager");
    Optional<ReleaseFeedDto.Release> latestRelease = parseLatestReleaseFromRssFeed(url);
    return latestRelease.map(release -> new ReleaseInfo(release.getTitle(), release.getLink()));
  }

  private Optional<ReleaseFeedDto.Release> parseLatestReleaseFromRssFeed(String url) {
    try {
      ReleaseFeedDto releaseFeed = client.get(url).request().contentFromXml(ReleaseFeedDto.class);
      return filterForLatestRelease(releaseFeed);
    } catch (IOException e) {
      LOG.error(String.format("Could not parse release feed from %s", url));
      return Optional.empty();
    }
  }

  private Optional<ReleaseFeedDto.Release> filterForLatestRelease(ReleaseFeedDto releaseFeed) {
    return releaseFeed.getChannel().getReleases()
      .stream()
      .max(Comparator.comparing(ReleaseFeedDto.Release::getPubDate));
  }
}
