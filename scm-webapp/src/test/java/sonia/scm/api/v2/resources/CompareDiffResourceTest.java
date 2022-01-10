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

import de.otto.edison.hal.Links;
import org.github.sdorra.jse.ShiroExtension;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.DiffCommandBuilder;
import sonia.scm.repository.api.DiffResult;
import sonia.scm.repository.api.DiffResultCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.web.RestDispatcher;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompareDiffResourceTest extends RepositoryTestBase {

  private final Repository repository = RepositoryTestData.create42Puzzle();
  private final RestDispatcher dispatcher = new RestDispatcher();

  @Mock
  private DiffResultToDiffResultDtoMapper mapper;
  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock
  private RepositoryService service;
  @Mock(answer = Answers.RETURNS_SELF)
  private DiffCommandBuilder diffCommandBuilder;
  @Mock(answer = Answers.RETURNS_SELF)
  private DiffResultCommandBuilder diffResultCommandBuilder;

  @InjectMocks
  private CompareDiffResource resource;

  @BeforeEach
  void init() {
    super.compareDiffResource = resource;
    dispatcher.addSingletonResource(getRepositoryRootResource());
    when(serviceFactory.create(repository.getNamespaceAndName())).thenReturn(service);
  }

  @Test
  void shouldGetComparedDiff() throws URISyntaxException, IOException {
    when(service.getDiffCommand()).thenReturn(diffCommandBuilder);
    when(diffCommandBuilder.retrieveContent()).thenReturn(os -> {
    });

    MockHttpRequest request = MockHttpRequest.get("/v2/repositories/" + repository.getNamespaceAndName() + "/compare-diff/master/develop/");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test
  void shouldGetComparedParsedDiff() throws URISyntaxException, IOException {
    when(service.getDiffResultCommand()).thenReturn(diffResultCommandBuilder);
    when(service.getRepository()).thenReturn(repository);
    DiffResult mockedDiffResult = mock(DiffResult.class);
    when(diffResultCommandBuilder.getDiffResult()).thenReturn(mockedDiffResult);

    DiffResultDto diffResultDto = new DiffResultDto(Links.emptyLinks());
    diffResultDto.setPartial(false);
    diffResultDto.setFiles(Collections.singletonList(new DiffResultDto.FileDto(Links.emptyLinks())));
    when(mapper.mapForRevision(repository, mockedDiffResult, "master")).thenReturn(diffResultDto);


    MockHttpRequest request = MockHttpRequest.get("/v2/repositories/" + repository.getNamespaceAndName() + "/compare-diff/master/develop/parsed/");
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(200);
  }
}
