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

import com.google.common.io.Resources;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.api.v2.resources.RepositoryImportResource.RepositoryImportFromFileDto;
import sonia.scm.importexport.FromBundleImporter;
import sonia.scm.importexport.FromUrlImporter;
import sonia.scm.importexport.FullScmRepositoryImporter;
import sonia.scm.importexport.RepositoryImportExportEncryption;
import sonia.scm.importexport.RepositoryImportLoggerFactory;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.web.RestDispatcher;
import sonia.scm.web.VndMediaType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static jakarta.servlet.http.HttpServletResponse.SC_CREATED;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("UnstableApiUsage")
@RunWith(MockitoJUnitRunner.class)
public class RepositoryImportResourceTest extends RepositoryTestBase {

  private final RestDispatcher dispatcher = new RestDispatcher();

  @Mock
  private FullScmRepositoryImporter fullScmRepositoryImporter;
  @Mock
  private FromUrlImporter fromUrlImporter;
  @Mock
  private FromBundleImporter fromBundleImporter;
  @Mock
  private RepositoryImportLoggerFactory importLoggerFactory;
  @Mock
  private RepositoryImportExportEncryption repositoryImportExportEncryption;

  @Captor
  private ArgumentCaptor<FromUrlImporter.RepositoryImportParameters> parametersCaptor;
  @Captor
  private ArgumentCaptor<Repository> repositoryCaptor;

  private final URI baseUri = URI.create("/");
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @InjectMocks
  private RepositoryDtoToRepositoryMapperImpl dtoToRepositoryMapper;

  private final MockHttpResponse response = new MockHttpResponse();

  @Before
  public void prepareEnvironment() {
    super.repositoryImportResource = new RepositoryImportResource(dtoToRepositoryMapper, resourceLinks, fullScmRepositoryImporter, new RepositoryImportDtoToRepositoryImportParametersMapperImpl(), repositoryImportExportEncryption, fromUrlImporter, fromBundleImporter, importLoggerFactory);
    dispatcher.addSingletonResource(getRepositoryRootResource());
  }

  @Test
  public void shouldImportRepositoryFromUrl() throws Exception {
    when(fromUrlImporter.importFromUrl(parametersCaptor.capture(), repositoryCaptor.capture()))
      .thenReturn(RepositoryTestData.createHeartOfGold());

    URL url = Resources.getResource("sonia/scm/api/v2/import-repo.json");
    byte[] importRequest = Resources.toByteArray(url);

    MockHttpRequest request = MockHttpRequest
      .post("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "import/git/url")
      .contentType(VndMediaType.REPOSITORY)
      .content(importRequest);

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(SC_CREATED);
    assertThat(response.getOutputHeaders().get("Location")).asString().contains("/v2/repositories/hitchhiker/HeartOfGold");

    assertThat(parametersCaptor.getValue().getImportUrl()).isEqualTo("https://scm-manager-org/scm/repo/secret/puzzle42");
    assertThat(parametersCaptor.getValue().getUsername()).isNull();
    assertThat(parametersCaptor.getValue().getPassword()).isNull();

    assertThat(repositoryCaptor.getValue().getName()).isEqualTo("HeartOfGold");
    assertThat(repositoryCaptor.getValue().getNamespace()).isEqualTo("hitchhiker");
  }

  @Test
  public void shouldImportRepositoryFromUrlWithCredentials() throws Exception {
    when(fromUrlImporter.importFromUrl(parametersCaptor.capture(), repositoryCaptor.capture()))
      .thenReturn(RepositoryTestData.createHeartOfGold());

    URL url = Resources.getResource("sonia/scm/api/v2/import-repo-with-credentials.json");
    byte[] importRequest = Resources.toByteArray(url);

    MockHttpRequest request = MockHttpRequest
      .post("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "import/git/url")
      .contentType(VndMediaType.REPOSITORY)
      .content(importRequest);

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(SC_CREATED);

    assertThat(parametersCaptor.getValue().getUsername()).isEqualTo("trillian");
    assertThat(parametersCaptor.getValue().getPassword()).isEqualTo("secret");
  }

  @Test
  public void shouldFailOnImportFromUrlWithDifferentTypes() throws Exception {
    URL url = Resources.getResource("sonia/scm/api/v2/import-repo.json");
    byte[] importRequest = Resources.toByteArray(url);

    MockHttpRequest request = MockHttpRequest
      .post("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "import/svn/url")
      .contentType(VndMediaType.REPOSITORY)
      .content(importRequest);

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isNotEqualTo(SC_CREATED);

    verify(fromUrlImporter, never()).importFromUrl(any(), any());
  }

  @Nested
  class WithCorrectBundle {

    @BeforeEach
    void mockImporter() {
      when(
        fromBundleImporter.importFromBundle(
          eq(false),
          argThat(argument -> streamHasContent(argument, "svn-dump")),
          argThat(repository -> repository.getName().equals("HeartOfGold"))
        )
      ).thenReturn(RepositoryTestData.createHeartOfGold());
    }

    @Test
    public void shouldImportRepositoryFromBundle() throws Exception {
      RepositoryImportFromFileDto importDto = createBasicImportDto();

      MockHttpRequest request = MockHttpRequest
        .post("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "import/svn/bundle");

      MultiPartRequestBuilder.multipartRequest(request, singletonMap("bundle", new ByteArrayInputStream("svn-dump".getBytes(UTF_8))), importDto);

      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(SC_CREATED);
      assertThat(response.getOutputHeaders().get("Location")).asString().contains("/v2/repositories/hitchhiker/HeartOfGold");
      verify(repositoryImportExportEncryption, never()).decrypt(any(), any());
    }

    @Test
    public void shouldImportRepositoryFromEncryptedBundle() throws Exception {
      when(repositoryImportExportEncryption.decrypt(any(), eq("hgt2g")))
        .thenAnswer(invocation -> invocation.getArgument(0));

      RepositoryImportFromFileDto importDto = createBasicImportDto();
      importDto.setPassword("hgt2g");

      MockHttpRequest request = MockHttpRequest
        .post("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "import/svn/bundle");

      MultiPartRequestBuilder.multipartRequest(request, singletonMap("bundle", new ByteArrayInputStream("svn-dump".getBytes(UTF_8))), importDto);

      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(SC_CREATED);
      assertThat(response.getOutputHeaders().get("Location")).asString().contains("/v2/repositories/hitchhiker/HeartOfGold");
    }

    private RepositoryImportFromFileDto createBasicImportDto() {
      RepositoryImportFromFileDto importDto = new RepositoryImportFromFileDto();
      importDto.setName("HeartOfGold");
      importDto.setNamespace("hitchhiker");
      importDto.setType("svn");
      return importDto;
    }
  }

  @Test
  public void shouldFailOnImportFromBundleWithDifferentTypes() throws Exception {
    RepositoryDto repositoryDto = new RepositoryDto();
    repositoryDto.setName("HeartOfGold");
    repositoryDto.setNamespace("hitchhiker");
    repositoryDto.setType("svn");

    MockHttpRequest request = MockHttpRequest
      .post("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "import/git/bundle");

    MultiPartRequestBuilder.multipartRequest(request, Collections.singletonMap("bundle", new ByteArrayInputStream("svn-dump".getBytes(StandardCharsets.UTF_8))), repositoryDto);

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isNotEqualTo(SC_CREATED);
    verify(fromBundleImporter, never()).importFromBundle(any(Boolean.class), any(InputStream.class), any(Repository.class));
  }

  @Test
  public void shouldImportFullRepository() throws Exception {
    when(
      fullScmRepositoryImporter.importFromStream(
        argThat(repository -> repository.getName().equals("HeartOfGold")),
        argThat(argument -> streamHasContent(argument, "svn-dump")),
        isNull()
      )
    ).thenReturn(RepositoryTestData.createHeartOfGold());

    RepositoryDto repositoryDto = new RepositoryDto();
    repositoryDto.setName("HeartOfGold");
    repositoryDto.setNamespace("hitchhiker");
    repositoryDto.setType("svn");

    MockHttpRequest request = MockHttpRequest
      .post("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "import/svn/full");

    MultiPartRequestBuilder.multipartRequest(request, singletonMap("bundle", new ByteArrayInputStream("svn-dump".getBytes(UTF_8))), repositoryDto);

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(SC_CREATED);
    assertThat(response.getOutputHeaders().get("Location")).asString().contains("/v2/repositories/hitchhiker/HeartOfGold");
  }

  @Test
  public void shouldFindImportLog() throws Exception {
    doAnswer(
      invocation -> {
        invocation.getArgument(1, OutputStream.class).write("some log".getBytes(UTF_8));
        return null;
      }
    ).when(importLoggerFactory).getLog(eq("42"), any(OutputStream.class));

    MockHttpRequest request = MockHttpRequest
      .get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "import/log/42");

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(SC_OK);
    assertThat(response.getContentAsString()).isEqualTo("some log");
    verify(importLoggerFactory).checkCanReadLog("42");
  }

  private boolean streamHasContent(InputStream argument, String expectedContent) {
    try {
      byte[] data = new byte[expectedContent.length()];
      argument.read(data);
      return new String(data).equals(expectedContent);
    } catch (IOException e) {
      return false;
    }
  }

}
