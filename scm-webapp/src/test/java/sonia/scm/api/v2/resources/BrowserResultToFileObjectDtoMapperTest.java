package sonia.scm.api.v2.resources;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.util.ThreadState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.NamespaceAndName;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class BrowserResultToFileObjectDtoMapperTest {

  private final URI baseUri = URI.create("http://example.com/base/");
  @SuppressWarnings("unused") // Is injected
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @Mock
  private FileObjectToFileObjectDtoMapper fileObjectToFileObjectDtoMapper;

  @InjectMocks
  private BrowserResultToFileObjectDtoMapper mapper;

  private final Subject subject = mock(Subject.class);
  private final ThreadState subjectThreadState = new SubjectThreadState(subject);

  private FileObject fileObject1 = new FileObject();
  private FileObject fileObject2 = new FileObject();


  @Before
  public void init() {
    initMocks(this);
    subjectThreadState.bind();
    ThreadContext.bind(subject);

    fileObject1.setName("FO 1");
    fileObject1.setLength(100);
    fileObject1.setLastModified(0L);
    fileObject1.setPath("/path/object/1");
    fileObject1.setDescription("description of file object 1");
    fileObject1.setDirectory(false);

    fileObject2.setName("FO 2");
    fileObject2.setLength(100);
    fileObject2.setLastModified(101L);
    fileObject2.setPath("/path/object/2");
    fileObject2.setDescription("description of file object 2");
    fileObject2.setDirectory(true);

    when(fileObjectToFileObjectDtoMapper.map(any(FileObject.class), any(NamespaceAndName.class), anyString()))
      .thenReturn(new FileObjectDto());
  }

  @After
  public void unbind() {
    ThreadContext.unbindSubject();
  }

  @Test
  public void shouldMapAttributesCorrectly() {
    BrowserResult browserResult = createBrowserResult();

    FileObjectDto dto = mapper.map(browserResult, new NamespaceAndName("foo", "bar"), "path");

    assertEqualAttributes(browserResult, dto);
  }

  @Test
  public void shouldDelegateToFileObjectsMapper() {
    BrowserResult browserResult = createBrowserResult();
    NamespaceAndName namespaceAndName = new NamespaceAndName("foo", "bar");

    FileObjectDto dto = mapper.map(browserResult, namespaceAndName, "path");

    verify(fileObjectToFileObjectDtoMapper).map(fileObject1, namespaceAndName, "Revision");
    verify(fileObjectToFileObjectDtoMapper).map(fileObject2, namespaceAndName, "Revision");
  }

  @Test
  public void shouldSetLinksCorrectly() {
    BrowserResult browserResult = createBrowserResult();
    NamespaceAndName namespaceAndName = new NamespaceAndName("foo", "bar");

    FileObjectDto dto = mapper.map(browserResult, namespaceAndName, "path");

    assertThat(dto.getLinks().getLinkBy("self").get().getHref()).contains("path");
  }

  private BrowserResult createBrowserResult() {
    return new BrowserResult("Revision", createFileObject());
  }

  private FileObject createFileObject() {
    FileObject file = new FileObject();
    file.setName("");
    file.setPath("");
    file.setDirectory(true);

    file.addChild(fileObject1);
    file.addChild(fileObject2);
    return file;
  }

  private void assertEqualAttributes(BrowserResult browserResult, FileObjectDto dto) {
    assertThat(dto.getRevision()).isEqualTo(browserResult.getRevision());
  }

}
