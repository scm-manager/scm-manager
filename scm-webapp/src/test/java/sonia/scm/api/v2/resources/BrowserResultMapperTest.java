package sonia.scm.api.v2.resources;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.util.ThreadState;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class BrowserResultMapperTest {

  private final URI baseUri = URI.create("http://example.com/base/");
  @SuppressWarnings("unused") // Is injected
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @Mock
  private FileObjectMapper fileObjectMapper;

  @InjectMocks
  private BrowserResultMapper mapper;

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
  }

  @Test
  public void shouldMapAttributesCorrectly() {
    BrowserResult browserResult = createBrowserResult();

    BrowserResultDto dto = mapper.map(browserResult, new NamespaceAndName("foo", "bar"));

    assertEqualAttributes(browserResult, dto);
  }

  @Test
  public void shouldDelegateToFileObjectsMapper() {
    BrowserResult browserResult = createBrowserResult();
    NamespaceAndName namespaceAndName = new NamespaceAndName("foo", "bar");

    BrowserResultDto dto = mapper.map(browserResult, namespaceAndName);

    verify(fileObjectMapper).map(fileObject1, namespaceAndName, "Revision");
    verify(fileObjectMapper).map(fileObject2, namespaceAndName, "Revision");
  }

  private BrowserResult createBrowserResult() {
    BrowserResult browserResult = new BrowserResult();
    browserResult.setTag("Tag");
    browserResult.setRevision("Revision");
    browserResult.setBranch("Branch");
    browserResult.setFiles(createFileObjects());

    return browserResult;
  }

  private List<FileObject> createFileObjects() {
    List<FileObject> fileObjects = new ArrayList<>();

    fileObjects.add(fileObject1);
    fileObjects.add(fileObject2);
    return fileObjects;
  }

  private void assertEqualAttributes(BrowserResult browserResult, BrowserResultDto dto) {
    assertThat(dto.getTag()).isEqualTo(browserResult.getTag());
    assertThat(dto.getBranch()).isEqualTo(browserResult.getBranch());
    assertThat(dto.getRevision()).isEqualTo(browserResult.getRevision());
  }

}
