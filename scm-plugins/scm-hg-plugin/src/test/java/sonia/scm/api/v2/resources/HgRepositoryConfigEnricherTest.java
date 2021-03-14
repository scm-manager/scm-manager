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
 *
 */

package sonia.scm.api.v2.resources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import java.net.URI;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HgRepositoryConfigEnricherTest {

  private HgRepositoryConfigEnricher enricher;

  @Mock
  private HalEnricherContext context;

  @Mock
  private HalAppender appender;

  @BeforeEach
  void setUp() {
    ScmPathInfoStore store = new ScmPathInfoStore();
    store.set(() -> URI.create("/"));
    HgConfigLinks links = new HgConfigLinks(store);
    enricher = new HgRepositoryConfigEnricher(links);
  }

  @Test
  void shouldEnrichHgRepository() {
    addRepositoryToContext("hg");
    enricher.enrich(context, appender);
    verify(appender).appendLink("configuration", "/v2/config/hg/hitchhiker/HeartOfGold");
  }

  @Test
  void shouldNotEnrichNonHgRepository() {
    addRepositoryToContext("git");
    enricher.enrich(context, appender);
    verify(appender, never()).appendLink(anyString(), anyString());
  }

  private void addRepositoryToContext(String type) {
    Repository repository = RepositoryTestData.createHeartOfGold(type);
    when(context.oneRequireByType(Repository.class)).thenReturn(repository);
  }

}
