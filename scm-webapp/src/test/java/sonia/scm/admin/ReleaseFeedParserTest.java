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

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.net.ahc.AdvancedHttpClient;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReleaseFeedParserTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  AdvancedHttpClient client;

  @InjectMocks
  ReleaseFeedParser releaseFeedParser;

  @Test
  void shouldFindLatestRelease() throws IOException {
    String url = "https://www.scm-manager.org/download/rss.xml";

    when(client.get(url).request().contentFromXml(ReleaseFeedDto.class)).thenReturn(createReleaseFeedDto());

    Optional<ReleaseInfo> release = releaseFeedParser.findLatestRelease(url);

    assertThat(release).isPresent();
    assertThat(release.get().getTitle()).isEqualTo("3");
    assertThat(release.get().getTitle()).isEqualTo("download-3");
  }

  private ReleaseFeedDto createReleaseFeedDto() {
    ReleaseFeedDto.Release release1 = createRelease("1", "download-1", new Date(1000000000L));
    ReleaseFeedDto.Release release2 = createRelease("2", "download-2", new Date(2000000000L));
    ReleaseFeedDto.Release release3 = createRelease("3", "download-3", new Date(3000000000L));
    ReleaseFeedDto.Channel channel = new ReleaseFeedDto.Channel("scm", "scm releases", "scm-download", "gatsby", new Date(1L), ImmutableList.of(release1, release2, release3));
    return new ReleaseFeedDto(channel);
  }

  private ReleaseFeedDto.Release createRelease(String version, String link, Date date) {
    return new ReleaseFeedDto.Release(version, version, link, version, date);
  }
}
