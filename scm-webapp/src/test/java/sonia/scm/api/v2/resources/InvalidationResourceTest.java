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
    void shouldReIndexAll() throws URISyntaxException {
      MockHttpResponse response = invokeReIndex();

      assertThat(response.getStatus()).isEqualTo(204);

      verify(indexRebuilder).rebuildAll();
    }

    private MockHttpResponse invokeReIndex() throws URISyntaxException {
      MockHttpRequest request = MockHttpRequest.post(basePath + "/search-index");
      MockHttpResponse response = new MockHttpResponse();

      dispatcher.invoke(request, response);
      return response;
    }
  }
}
