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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.BlameLine;
import sonia.scm.repository.BlameResult;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Person;
import sonia.scm.repository.api.BlameCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.web.RestDispatcher;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnnotateResourceTest extends RepositoryTestBase {

  public static final NamespaceAndName NAMESPACE_AND_NAME = new NamespaceAndName("space", "X");
  public static final String REVISION = "123";
  public static final String PATH = "some/file";
  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock
  private RepositoryService service;
  @Mock
  private BlameCommandBuilder blameCommandBuilder;

  private final RestDispatcher dispatcher = new RestDispatcher();
  private final MockHttpResponse response = new MockHttpResponse();

  @BeforeEach
  void initResource() {
    BlameResultToBlameDtoMapperImpl mapper = new BlameResultToBlameDtoMapperImpl();
    mapper.setResourceLinks(ResourceLinksMock.createMock(URI.create("/")));
    annotateResource = new AnnotateResource(serviceFactory, mapper);
    dispatcher.addSingletonResource(getRepositoryRootResource());
  }

  @BeforeEach
  void initRepositoryService() {
    when(serviceFactory.create(NAMESPACE_AND_NAME)).thenReturn(service);
    when(service.getBlameCommand()).thenReturn(blameCommandBuilder);
  }

  @BeforeEach
  void initBlameCommand() throws IOException {
    BlameLine line1 = new BlameLine(
      0,
      "100",
      System.currentTimeMillis(),
      new Person("Arthur Dent", "arthur@hitchhiker.com"),
      "first try",
      "jump"
    );
    BlameLine line2 = new BlameLine(
      1,
      "42",
      System.currentTimeMillis(),
      new Person("Zaphod Beeblebrox", "zaphod@hitchhiker.com"),
      "got it",
      "heart of gold"
    );
    BlameResult result = new BlameResult(asList(line1, line2));
    when(blameCommandBuilder.setRevision(REVISION)).thenReturn(blameCommandBuilder);
    when(blameCommandBuilder.getBlameResult(PATH)).thenReturn(result);
  }

  @Test
  void shouldReturnAnnotations() throws URISyntaxException, UnsupportedEncodingException, JsonProcessingException {
    MockHttpRequest request = MockHttpRequest
      .get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + NAMESPACE_AND_NAME + "/annotate/" + REVISION + "/" + PATH);

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(200);

    String content = response.getContentAsString();
    ObjectMapper mapper = new ObjectMapper();
    JsonNode jsonNode = mapper.readTree(content);
    JsonNode blameLines = jsonNode.get("lines");
    assertThat(blameLines.isArray()).isTrue();
    assertThat(jsonNode.get("_links").get("self").get("href").asText())
      .isEqualTo("/v2/repositories/space/X/annotate/123/some%2Ffile");

    Iterator<JsonNode> lineIterator = blameLines.iterator();
    JsonNode line1 = lineIterator.next();
    assertThat(line1.get("author").get("mail").asText()).isEqualTo("arthur@hitchhiker.com");
    assertThat(line1.get("author").get("name").asText()).isEqualTo("Arthur Dent");
    assertThat(line1.get("code").asText()).isEqualTo("jump");
    assertThat(line1.get("description").asText()).isEqualTo("first try");
    assertThat(line1.get("lineNumber").asInt()).isEqualTo(0);
    assertThat(line1.get("revision").asText()).isEqualTo("100");

    JsonNode line2 = lineIterator.next();
    assertThat(line2.get("author").get("mail").asText()).isEqualTo("zaphod@hitchhiker.com");
    assertThat(line2.get("author").get("name").asText()).isEqualTo("Zaphod Beeblebrox");
    assertThat(line2.get("code").asText()).isEqualTo("heart of gold");
    assertThat(line2.get("description").asText()).isEqualTo("got it");
    assertThat(line2.get("lineNumber").asInt()).isEqualTo(1);
    assertThat(line2.get("revision").asText()).isEqualTo("42");

    assertThat(lineIterator.hasNext()).isFalse();
  }
}
