package sonia.scm.api.v2.resources;

import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.PathNotFoundException;
import sonia.scm.repository.RepositoryNotFoundException;
import sonia.scm.repository.api.CatCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ContentResourceTest {

  private static final String NAMESPACE = "space";
  private static final String REPO_NAME = "name";
  private static final String REV = "rev";

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private RepositoryServiceFactory repositoryServiceFactory;

  @InjectMocks
  private ContentResource contentResource;

  private CatCommandBuilder catCommand;

  @Before
  public void initService() throws Exception {
    NamespaceAndName existingNamespaceAndName = new NamespaceAndName(NAMESPACE, REPO_NAME);
    RepositoryService repositoryService = repositoryServiceFactory.create(existingNamespaceAndName);
    catCommand = repositoryService.getCatCommand();
    when(catCommand.setRevision(REV)).thenReturn(catCommand);

    // defaults for unknown things
    doThrow(new RepositoryNotFoundException("x")).when(repositoryServiceFactory).create(not(eq(existingNamespaceAndName)));
    doThrow(new PathNotFoundException("x")).when(catCommand).retriveContent(any(), any());
  }

  @Test
  public void shouldReadSimpleFile() throws Exception {
    mockContent("file", "Hello".getBytes());

    Response response = contentResource.get(NAMESPACE, REPO_NAME, REV, "file");
    assertEquals(200, response.getStatus());

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ((StreamingOutput) response.getEntity()).write(baos);

    assertEquals("Hello", baos.toString());
  }

  @Test
  public void shouldHandleMissingFile() {
    Response response = contentResource.get(NAMESPACE, REPO_NAME, REV, "doesNotExist");
    assertEquals(404, response.getStatus());
  }

  @Test
  public void shouldHandleMissingRepository() {
    Response response = contentResource.get("no", "repo", REV, "anything");
    assertEquals(404, response.getStatus());
  }

  @Test
  public void shouldRecognizeTikaSourceCode() throws Exception {
    mockContentFromResource("SomeGoCode.go");

    Response response = contentResource.get(NAMESPACE, REPO_NAME, REV, "SomeGoCode.go");
    assertEquals(200, response.getStatus());

    assertEquals("GO", response.getHeaderString("Language"));
    assertEquals("text/x-go", response.getHeaderString("Content-Type"));
  }

  @Test
  public void shouldRecognizeSpecialSourceCode() throws Exception {
    mockContentFromResource("Dockerfile");

    Response response = contentResource.get(NAMESPACE, REPO_NAME, REV, "Dockerfile");
    assertEquals(200, response.getStatus());

    assertEquals("DOCKERFILE", response.getHeaderString("Language"));
    assertEquals("text/plain", response.getHeaderString("Content-Type"));
  }

  @Test
  public void shouldHandleRandomByteFile() throws Exception {
    mockContentFromResource("JustBytes");

    Response response = contentResource.get(NAMESPACE, REPO_NAME, REV, "JustBytes");
    assertEquals(200, response.getStatus());

    assertFalse(response.getHeaders().containsKey("Language"));
    assertEquals("application/octet-stream", response.getHeaderString("Content-Type"));
  }

  private void mockContentFromResource(String fileName) throws Exception {
    URL url = Resources.getResource(fileName);
    mockContent(fileName, Resources.toByteArray(url));
  }

  private void mockContent(String path, byte[] content) throws Exception {
    doAnswer(invocation -> {
      OutputStream outputStream = (OutputStream) invocation.getArguments()[0];
      outputStream.write(content);
      outputStream.close();
      return null;
    }).when(catCommand).retriveContent(any(), eq(path));
  }
}
