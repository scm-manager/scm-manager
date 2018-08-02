package sonia.scm.api.v2.resources;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.util.ThreadState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import sonia.scm.repository.HgConfig;

import java.io.File;
import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HgConfigToHgConfigDtoMapperTest {

  private URI baseUri = URI.create("http://example.com/base/");

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private UriInfoStore uriInfoStore;

  @InjectMocks
  private HgConfigToHgConfigDtoMapperImpl mapper;

  private final Subject subject = mock(Subject.class);
  private final ThreadState subjectThreadState = new SubjectThreadState(subject);

  private URI expectedBaseUri;

  @Before
  public void init() {
    when(uriInfoStore.get().getBaseUri()).thenReturn(baseUri);
    expectedBaseUri = baseUri.resolve(HgConfigResource.GIT_CONFIG_PATH_V2);
    subjectThreadState.bind();
    ThreadContext.bind(subject);
  }

  @After
  public void unbindSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  public void shouldMapFields() {
    HgConfig config = createConfiguration();

    when(subject.isPermitted("configuration:write:hg")).thenReturn(true);
    HgConfigDto dto = mapper.map(config);

    assertTrue(dto.isDisabled());
    assertEquals("repository/directory", dto.getRepositoryDirectory().getPath());

    assertEquals("ABC", dto.getEncoding());
    assertEquals("/etc/hg", dto.getHgBinary());
    assertEquals("/py", dto.getPythonBinary());
    assertEquals("/etc/", dto.getPythonPath());
    assertTrue(dto.isShowRevisionInId());
    assertTrue(dto.isUseOptimizedBytecode());

    assertEquals(expectedBaseUri.toString(), dto.getLinks().getLinkBy("self").get().getHref());
    assertEquals(expectedBaseUri.toString(), dto.getLinks().getLinkBy("update").get().getHref());
  }

  @Test
  public void shouldMapFieldsWithoutUpdate() {
    HgConfig config = createConfiguration();

    when(subject.isPermitted("configuration:write:hg")).thenReturn(false);
    HgConfigDto dto = mapper.map(config);

    assertEquals(expectedBaseUri.toString(), dto.getLinks().getLinkBy("self").get().getHref());
    assertFalse(dto.getLinks().hasLink("update"));
  }

  private HgConfig createConfiguration() {
    HgConfig config = new HgConfig();
    config.setDisabled(true);
    config.setRepositoryDirectory(new File("repository/directory"));

    config.setEncoding("ABC");
    config.setHgBinary("/etc/hg");
    config.setPythonBinary("/py");
    config.setPythonPath("/etc/");
    config.setShowRevisionInId(true);
    config.setUseOptimizedBytecode(true);
    
    return config;
  }

}
