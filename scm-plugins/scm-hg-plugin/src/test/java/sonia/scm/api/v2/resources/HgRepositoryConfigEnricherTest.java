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
