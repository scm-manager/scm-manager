package sonia.scm.api.v2.resources;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.util.ThreadState;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.security.Role;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static sonia.scm.api.v2.resources.GlobalConfigResourceTest.createConfiguration;

public class ScmConfigurationToGlobalConfigDtoMapperTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ResourceLinks resourceLinks;

  @InjectMocks
  private ScmConfigurationToGlobalConfigDtoMapperImpl mapper;

  private final Subject subject = mock(Subject.class);
  private final ThreadState subjectThreadState = new SubjectThreadState(subject);

  private URI expectedBaseUri;

  @Before
  public void init() throws URISyntaxException {
    initMocks(this);
    URI baseUri = new URI("http://example.com/base/");
    expectedBaseUri = baseUri.resolve(GlobalConfigResource.GLOBAL_CONFIG_PATH_V2);
    subjectThreadState.bind();
    ResourceLinksMock.initMock(resourceLinks, baseUri);
    ThreadContext.bind(subject);
  }

  @Test
  public void shouldMapFields() {
    ScmConfiguration config = createConfiguration();

    when(subject.hasRole(Role.ADMIN)).thenReturn(true);
    GlobalConfigDto dto = mapper.map(config);

    assertEquals("baseurl", dto.getBaseUrl());
    assertEquals(expectedBaseUri.toString(), dto.getLinks().getLinkBy("self").get().getHref());
    assertEquals(expectedBaseUri.toString(), dto.getLinks().getLinkBy("update").get().getHref());
  }

  @Test
  public void shouldMapFieldsWithoutUpdate() {
    ScmConfiguration config = createConfiguration();

    when(subject.hasRole(Role.ADMIN)).thenReturn(false);
    GlobalConfigDto dto = mapper.map(config);

    assertEquals("baseurl", dto.getBaseUrl());
    assertEquals(expectedBaseUri.toString(), dto.getLinks().getLinkBy("self").get().getHref());
    assertFalse(dto.getLinks().hasLink("update"));
  }

}
