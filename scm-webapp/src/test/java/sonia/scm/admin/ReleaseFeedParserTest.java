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
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpResponse;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static sonia.scm.admin.ReleaseFeedParser.SPAN_KIND;

@ExtendWith(MockitoExtension.class)
class ReleaseFeedParserTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  AdvancedHttpClient client;

  ReleaseFeedParser releaseFeedParser;

  @BeforeEach
  void createSut() {
    releaseFeedParser = new ReleaseFeedParser(client, new SimpleMeterRegistry(), 500);
  }

  @Test
  void shouldFindLatestRelease() throws IOException {
    String url = "https://www.scm-manager.org/download/rss.xml";

    when(request(url).contentFromXml(ReleaseFeedDto.class)).thenReturn(createReleaseFeedDto());

    Optional<UpdateInfo> update = releaseFeedParser.findLatestRelease(url);

    assertThat(update).isPresent();
    assertThat(update.get().getLatestVersion()).isEqualTo("3");
    assertThat(update.get().getLink()).isEqualTo("download-3");
  }

  private AdvancedHttpResponse request(String url) throws IOException {
    return client.get(url).spanKind(SPAN_KIND).request();
  }

  @Test
  void shouldHandleTimeout() throws IOException {
    String url = "https://www.scm-manager.org/download/rss.xml";

    Semaphore waitWithResultUntilTimeout = new Semaphore(0);

    when(request(url).contentFromXml(ReleaseFeedDto.class)).thenAnswer(invocation -> {
      waitWithResultUntilTimeout.acquire();
      return createReleaseFeedDto();
    });

    Optional<UpdateInfo> update = releaseFeedParser.findLatestRelease(url);

    waitWithResultUntilTimeout.release();

    assertThat(update).isEmpty();
  }

  @Test
  void shouldNotQueryInParallel() throws IOException, ExecutionException, InterruptedException {
    String url = "https://www.scm-manager.org/download/rss.xml";

    Semaphore waitWithResultUntilBothTriggered = new Semaphore(0);

    when(request(url).contentFromXml(ReleaseFeedDto.class)).thenAnswer(invocation -> {
      waitWithResultUntilBothTriggered.acquire();
      return createReleaseFeedDto();
    });

    final ExecutorService executorService = Executors.newFixedThreadPool(2);
    Future<Optional<UpdateInfo>> update1 = executorService.submit(() -> releaseFeedParser.findLatestRelease(url));
    Future<Optional<UpdateInfo>> update2 = executorService.submit(() -> releaseFeedParser.findLatestRelease(url));

    waitWithResultUntilBothTriggered.release(2);

    assertThat(update1.get()).containsSame(update2.get().get());
  }

  private ReleaseFeedDto createReleaseFeedDto() {
    ReleaseFeedDto.Release release1 = createRelease("1", "download-1", new Date(1000000000L));
    ReleaseFeedDto.Release release2 = createRelease("2", "download-2", new Date(2000000000L));
    ReleaseFeedDto.Release release3 = createRelease("3", "download-3", new Date(3000000000L));
    ReleaseFeedDto.Channel channel = new ReleaseFeedDto.Channel(ImmutableList.of(release1, release2, release3));
    return new ReleaseFeedDto(channel);
  }

  private ReleaseFeedDto.Release createRelease(String version, String link, Date date) {
    return new ReleaseFeedDto.Release(version, version, link, version, date);
  }
}
