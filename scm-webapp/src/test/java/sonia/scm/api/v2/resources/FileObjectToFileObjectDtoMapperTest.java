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

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
public class FileObjectToFileObjectDtoMapperTest {
  private final URI baseUri = URI.create("http://example.com/base/");

  @InjectMocks
  private FileObjectToFileObjectDtoMapperImpl mapper;

  private final Subject subject = mock(Subject.class);
  private final ThreadState subjectThreadState = new SubjectThreadState(subject);


  @Before
  public void init() throws URISyntaxException {
    initMocks(this);
    subjectThreadState.bind();
    ThreadContext.bind(subject);
  }


  @Test
  public void shouldMapAttributesCorrectly() {
    FileObject fileObject = createFileObject();
    FileObjectDto dto = mapper.map(fileObject);

    assertEqualAttributes(fileObject, dto);
  }


  private FileObject createFileObject() {
    FileObject fileObject = new FileObject();
    fileObject.setName("foo");
    fileObject.setDescription("bar");
    fileObject.setPath("/foo/bar");
    fileObject.setDirectory(false);
    fileObject.setLength(100);
    fileObject.setLastModified(123L);
    return fileObject;
  }

  //TODO: subrepo
  private void assertEqualAttributes(FileObject fileObject, FileObjectDto dto) {
    assertEquals(fileObject.getName(), dto.getName());
    assertEquals(fileObject.getDescription(), dto.getDescription());
    assertEquals(fileObject.getPath(), dto.getPath());
    assertEquals(fileObject.isDirectory(), dto.isDirectory());
    assertEquals(fileObject.getLength(), dto.getLength());
    assertEquals((long)fileObject.getLastModified(), dto.getLastModified().toEpochMilli());
  }
}
