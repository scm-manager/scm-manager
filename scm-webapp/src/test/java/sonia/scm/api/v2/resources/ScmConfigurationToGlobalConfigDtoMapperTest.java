package sonia.scm.api.v2.resources;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.util.ThreadState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.security.Role;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static sonia.scm.api.v2.resources.GlobalConfigResourceTest.createConfiguration;

public class ScmConfigurationToGlobalConfigDtoMapperTest {

  private URI baseUri =  URI.create("http://example.com/base/");
  @SuppressWarnings("unused") // Is injected
  private ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @InjectMocks
  private ScmConfigurationToGlobalConfigDtoMapperImpl mapper;

  private final Subject subject = mock(Subject.class);
  private final ThreadState subjectThreadState = new SubjectThreadState(subject);

  private URI expectedBaseUri;

  @Before
  public void init() {
    initMocks(this);
    expectedBaseUri = baseUri.resolve(GlobalConfigResource.GLOBAL_CONFIG_PATH_V2);
    subjectThreadState.bind();
    ThreadContext.bind(subject);
  }

  @After
  public void unbindSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  public void shouldMapFields() {
    ScmConfiguration config = createConfiguration();

    when(subject.isPermitted("configuration:write:global")).thenReturn(true);
    GlobalConfigDto dto = mapper.map(config);

    assertEquals("baseurl", dto.getBaseUrl());
    assertEquals(expectedBaseUri.toString(), dto.getLinks().getLinkBy("self").get().getHref());
    assertEquals(expectedBaseUri.toString(), dto.getLinks().getLinkBy("update").get().getHref());
  }

  @Test
  public void shouldMapFieldsWithoutUpdate() {
    ScmConfiguration config = createConfiguration();

    when(subject.hasRole("configuration:write:global")).thenReturn(false);
    GlobalConfigDto dto = mapper.map(config);

    assertEquals("baseurl", dto.getBaseUrl());
    assertEquals(expectedBaseUri.toString(), dto.getLinks().getLinkBy("self").get().getHref());
    assertFalse(dto.getLinks().hasLink("update"));
  }

}
