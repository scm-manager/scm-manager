package sonia.scm.api.v2.resources;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.util.ThreadState;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.SubRepository;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.Silent.class)
public class FileObjectMapperTest {

  private final URI baseUri = URI.create("http://example.com/base/");
  @SuppressWarnings("unused") // Is injected
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @InjectMocks
  private FileObjectMapperImpl mapper;

  private final Subject subject = mock(Subject.class);
  private final ThreadState subjectThreadState = new SubjectThreadState(subject);

  private URI expectedBaseUri;

  @Before
  public void init() {
    expectedBaseUri = baseUri.resolve(RepositoryRootResource.REPOSITORIES_PATH_V2 + "/");
    subjectThreadState.bind();
    ThreadContext.bind(subject);
  }


  @Test
  public void shouldMapAttributesCorrectly() {
    FileObject fileObject = createFileObject();
    FileObjectDto dto = mapper.map(fileObject, new NamespaceAndName("namespace", "name"), "revision");

    assertEqualAttributes(fileObject, dto);
  }

  @Test
  public void shouldHaveCorrectSelfLink() {
    FileObject fileObject = createFileObject();
    FileObjectDto dto = mapper.map(fileObject, new NamespaceAndName("namespace", "name"), "revision");

    assertThat(dto.getLinks().getLinkBy("self").get().getHref()).isEqualTo(expectedBaseUri.resolve("namespace/name/sources/revision/foo").toString());
  }

  @Test
  public void shouldHaveCorrectContentLink() {
    FileObject fileObject = createFileObject();
    FileObjectDto dto = mapper.map(fileObject, new NamespaceAndName("namespace", "name"), "revision");

    assertThat(dto.getLinks().getLinkBy("content").get().getHref()).isEqualTo(expectedBaseUri.resolve("namespace/name/content/revision/foo").toString());
  }

  private FileObject createFileObject() {
    FileObject fileObject = new FileObject();
    fileObject.setName("foo");
    fileObject.setDescription("bar");
    fileObject.setPath("/foo/bar");
    fileObject.setDirectory(false);
    fileObject.setLength(100);
    fileObject.setLastModified(123L);

    fileObject.setSubRepository(new SubRepository("repo.url"));
    return fileObject;
  }

  private void assertEqualAttributes(FileObject fileObject, FileObjectDto dto) {
    assertThat(dto.getName()).isEqualTo(fileObject.getName());
    assertThat(dto.getDescription()).isEqualTo(fileObject.getDescription());
    assertThat(dto.getPath()).isEqualTo(fileObject.getPath());
    assertThat(dto.isDirectory()).isEqualTo(fileObject.isDirectory());
    assertThat(dto.getLength()).isEqualTo(fileObject.getLength());
    assertThat(dto.getLastModified().toEpochMilli()).isEqualTo((long) fileObject.getLastModified());
    assertThat(dto.getSubRepository().getBrowserUrl()).isEqualTo(fileObject.getSubRepository().getBrowserUrl());
  }
}
