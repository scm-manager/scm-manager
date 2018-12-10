package sonia.scm.api.v2.resources;

import com.google.common.io.Resources;
import com.google.inject.util.Providers;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.api.MergeCommandBuilder;
import sonia.scm.repository.api.MergeCommandResult;
import sonia.scm.repository.api.MergeDryRunCommandResult;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.spi.MergeCommand;
import sonia.scm.web.VndMediaType;

import java.net.URL;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MergeResourceTest extends RepositoryTestBase {

  public static final String MERGE_URL = "/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo/merge/";

  private Dispatcher dispatcher;
  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock
  private RepositoryService repositoryService;
  @Mock
  private MergeCommand mergeCommand;
  @InjectMocks
  private MergeCommandBuilder mergeCommandBuilder;
  private MergeResultToDtoMapperImpl mapper = new MergeResultToDtoMapperImpl();

  private MergeResource mergeResource;

  @BeforeEach
  void init() {
    mergeResource = new MergeResource(serviceFactory, mapper);
    super.mergeResource = Providers.of(mergeResource);
    dispatcher = DispatcherMock.createDispatcher(getRepositoryRootResource());
  }

  @Test
  void shouldHandleIllegalInput() throws Exception {
    URL url = Resources.getResource("sonia/scm/api/v2/mergeCommand_invalid.json");
    byte[] mergeCommandJson = Resources.toByteArray(url);

    MockHttpRequest request = MockHttpRequest
      .post(MERGE_URL + "dry-run/")
      .content(mergeCommandJson)
      .contentType(VndMediaType.MERGE_COMMAND);
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(400);
    System.out.println(response.getContentAsString());
  }

  @Nested
  class ExecutingMergeCommand {

    @BeforeEach
    void initRepository() {
      when(serviceFactory.create(new NamespaceAndName("space", "repo"))).thenReturn(repositoryService);
      when(repositoryService.getMergeCommand()).thenReturn(mergeCommandBuilder);
    }

    @Test
    void shouldHandleSuccessfulMerge() throws Exception {
      when(mergeCommand.merge(any())).thenReturn(MergeCommandResult.success());

      URL url = Resources.getResource("sonia/scm/api/v2/mergeCommand.json");
      byte[] mergeCommandJson = Resources.toByteArray(url);

      MockHttpRequest request = MockHttpRequest
        .post(MERGE_URL)
        .content(mergeCommandJson)
        .contentType(VndMediaType.MERGE_COMMAND);
      MockHttpResponse response = new MockHttpResponse();
      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    void shouldHandleFailedMerge() throws Exception {
      when(mergeCommand.merge(any())).thenReturn(MergeCommandResult.failure(asList("file1", "file2")));

      URL url = Resources.getResource("sonia/scm/api/v2/mergeCommand.json");
      byte[] mergeCommandJson = Resources.toByteArray(url);

      MockHttpRequest request = MockHttpRequest
        .post(MERGE_URL)
        .content(mergeCommandJson)
        .contentType(VndMediaType.MERGE_COMMAND);
      MockHttpResponse response = new MockHttpResponse();
      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(409);
      assertThat(response.getContentAsString()).contains("file1", "file2");
    }

    @Test
    void shouldHandleSuccessfulDryRun() throws Exception {
      when(mergeCommand.dryRun(any())).thenReturn(new MergeDryRunCommandResult(true));

      URL url = Resources.getResource("sonia/scm/api/v2/mergeCommand.json");
      byte[] mergeCommandJson = Resources.toByteArray(url);

      MockHttpRequest request = MockHttpRequest
        .post(MERGE_URL + "dry-run/")
        .content(mergeCommandJson)
        .contentType(VndMediaType.MERGE_COMMAND);
      MockHttpResponse response = new MockHttpResponse();
      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    void shouldHandleFailedDryRun() throws Exception {
      when(mergeCommand.dryRun(any())).thenReturn(new MergeDryRunCommandResult(false));

      URL url = Resources.getResource("sonia/scm/api/v2/mergeCommand.json");
      byte[] mergeCommandJson = Resources.toByteArray(url);

      MockHttpRequest request = MockHttpRequest
        .post(MERGE_URL + "dry-run/")
        .content(mergeCommandJson)
        .contentType(VndMediaType.MERGE_COMMAND);
      MockHttpResponse response = new MockHttpResponse();
      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(409);
    }
  }
}
