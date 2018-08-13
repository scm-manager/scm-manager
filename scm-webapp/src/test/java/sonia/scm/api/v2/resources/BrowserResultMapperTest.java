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
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
public class BrowserResultMapperTest {

  @InjectMocks
  private BrowserResultMapperImpl mapper;

  private final Subject subject = mock(Subject.class);
  private final ThreadState subjectThreadState = new SubjectThreadState(subject);


  @Before
  public void init() {
    initMocks(this);
    subjectThreadState.bind();
    ThreadContext.bind(subject);
  }

  @Test
  public void shouldMapAttributesCorrectly() {
    BrowserResult browserResult = createBrowserResult();

    BrowserResultDto dto = mapper.map(browserResult);

    assertEqualAttributes(browserResult, dto);

    assertEqualFileObjectAttributes(browserResult.getFiles().get(0), dto.getFiles().get(0));
    assertEqualFileObjectAttributes(browserResult.getFiles().get(1), dto.getFiles().get(1));
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

    FileObject fileObject1 = new FileObject();
    fileObject1.setName("FO 1");
    fileObject1.setLength(100);
    fileObject1.setLastModified(0L);
    fileObject1.setPath("/path/object/1");
    fileObject1.setDescription("description of file object 1");
    fileObject1.setDirectory(false);

    FileObject fileObject2 = new FileObject();
    fileObject2.setName("FO 2");
    fileObject2.setLength(100);
    fileObject2.setLastModified(101L);
    fileObject2.setPath("/path/object/2");
    fileObject2.setDescription("description of file object 2");
    fileObject2.setDirectory(true);

    fileObjects.add(fileObject1);
    fileObjects.add(fileObject2);
    return fileObjects;
  }

  private void assertEqualAttributes(BrowserResult browserResult, BrowserResultDto dto) {
    assertThat(dto.getTag()).isEqualTo(browserResult.getTag());
    assertThat(dto.getBranch()).isEqualTo(browserResult.getBranch());
    assertThat(dto.getRevision()).isEqualTo(browserResult.getRevision());
  }

  private void assertEqualFileObjectAttributes(FileObject fileObject, FileObjectDto dto) {
    assertThat(dto.getName()).isEqualTo(fileObject.getName());
    assertThat(dto.getLength()).isEqualTo(fileObject.getLength());
    assertThat(dto.getLastModified()).isEqualTo(Instant.ofEpochMilli(fileObject.getLastModified()));
    assertThat(dto.isDirectory()).isEqualTo(fileObject.isDirectory());
    assertThat(dto.getDescription()).isEqualTo(fileObject.getDescription());
    assertThat(dto.getPath()).isEqualTo(fileObject.getPath());
  }
}
