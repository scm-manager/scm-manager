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

import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.cache.CacheManager;
import sonia.scm.search.IndexRebuilder;
import sonia.scm.web.RestDispatcher;

import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;


@ExtendWith({MockitoExtension.class, ShiroExtension.class})
@SubjectAware("TrainerRed")
class InvalidationResourceTest {

  @Mock
  private CacheManager cacheManager;
  @Mock
  private IndexRebuilder indexRebuilder;

  private RestDispatcher dispatcher;

  private final String basePath = "/v2/invalidations";

  @BeforeEach
  void init() {
   InvalidationResource invalidationResource = new InvalidationResource(cacheManager, indexRebuilder);

   dispatcher = new RestDispatcher();
   dispatcher.addSingletonResource(invalidationResource);
  }

  @Nested
  class InvalidateCaches {

    @Test
    void shouldReturnForbiddenBecauseOfMissingPermission() throws URISyntaxException {
      MockHttpResponse response = invokeInvalidateCaches();
      assertThat(response.getStatus()).isEqualTo(403);
      verifyNoInteractions(cacheManager);
    }

    @Test
    @SubjectAware(permissions = {"configuration:write:global"})
    void shouldClearCaches() throws URISyntaxException {
      MockHttpResponse response = invokeInvalidateCaches();
      assertThat(response.getStatus()).isEqualTo(204);
      verify(cacheManager).clearAllCaches();
    }

    private MockHttpResponse invokeInvalidateCaches() throws URISyntaxException {
      MockHttpRequest request = MockHttpRequest.post(basePath + "/caches");
      MockHttpResponse response = new MockHttpResponse();

      dispatcher.invoke(request, response);
      return response;
    }
  }

  @Nested
  class ReIndex {

    @Test
    void shouldReturnForbiddenBecauseOfMissingPermission() throws URISyntaxException {
      MockHttpResponse response = invokeReIndex();

      assertThat(response.getStatus()).isEqualTo(403);

      verifyNoInteractions(indexRebuilder);
    }

    @Test
    @SubjectAware(permissions = {"configuration:write:global"})
    void shouldReIndexAllWithClearedCaches() throws URISyntaxException {
      MockHttpResponse response = invokeReIndex();

      assertThat(response.getStatus()).isEqualTo(204);

      verify(indexRebuilder).rebuildAll();
      verify(cacheManager).clearAllCaches();
    }

    private MockHttpResponse invokeReIndex() throws URISyntaxException {
      MockHttpRequest request = MockHttpRequest.post(basePath + "/search-index");
      MockHttpResponse response = new MockHttpResponse();

      dispatcher.invoke(request, response);
      return response;
    }
  }
}
