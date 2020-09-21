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

import sonia.scm.SCMContextProvider;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.version.Version;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Optional;

public class ReleaseVersionChecker {

  private final ReleaseFeedReader releaseFeedReader;
  private final ScmConfiguration scmConfiguration;
  private final SCMContextProvider scmContextProvider;

  @Inject
  public ReleaseVersionChecker(ReleaseFeedReader releaseFeedReader, ScmConfiguration scmConfiguration, SCMContextProvider scmContextProvider) {
    this.releaseFeedReader = releaseFeedReader;
    this.scmConfiguration = scmConfiguration;
    this.scmContextProvider = scmContextProvider;
  }

  Optional<ReleaseInfo> checkForNewerVersion() {
    try {
      String releaseFeedUrl = scmConfiguration.getReleaseFeedUrl();
      Optional<ReleaseInfo> latestRelease = releaseFeedReader.findLatestRelease(releaseFeedUrl);
      if (latestRelease.isPresent()) {
        if (isNewerVersion(latestRelease.get())) {
          return latestRelease;
        }
      }
    } catch (IOException e) {
      // This is an silent action. We don't want the user to get any kind of error for this.
      return Optional.empty();
    }
    return Optional.empty();
  }

  private boolean isNewerVersion(ReleaseInfo releaseInfo) {
    Version versionFromReleaseFeed = Version.parse(releaseInfo.getTitle());
    return versionFromReleaseFeed.isNewer(scmContextProvider.getVersion());
  }
}
