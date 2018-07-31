package sonia.scm.api.v2.resources;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.util.ThreadState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.internal.util.collections.Sets;
import sonia.scm.config.ScmConfiguration;

import java.net.URI;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ScmConfigurationToConfigDtoMapperTest {

  private URI baseUri =  URI.create("http://example.com/base/");

  private String[] expectedUsers = { "trillian", "arthur" };
  private String[] expectedGroups = { "admin", "plebs" };
  private String[] expectedExcludes = { "ex", "clude" };

  @SuppressWarnings("unused") // Is injected
  private ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @InjectMocks
  private ScmConfigurationToConfigDtoMapperImpl mapper;

  private final Subject subject = mock(Subject.class);
  private final ThreadState subjectThreadState = new SubjectThreadState(subject);

  private URI expectedBaseUri;

  @Before
  public void init() {
    initMocks(this);
    expectedBaseUri = baseUri.resolve(ConfigResource.CONFIG_PATH_V2);
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
    ConfigDto dto = mapper.map(config);

    assertEquals("heartOfGold" , dto.getProxyPassword());
    assertEquals(1234 , dto.getProxyPort());
    assertEquals("proxyserver" , dto.getProxyServer());
    assertEquals("trillian" , dto.getProxyUser());
    assertTrue(dto.isEnableProxy());
    assertEquals("description" , dto.getRealmDescription());
    assertTrue(dto.isEnableRepositoryArchive());
    assertTrue(dto.isDisableGroupingGrid());
    assertEquals("dd" , dto.getDateFormat());
    assertTrue(dto.isAnonymousAccessEnabled());
    assertTrue("adminGroups", dto.getAdminGroups().containsAll(Arrays.asList(expectedGroups)));
    assertTrue("adminUsers", dto.getAdminUsers().containsAll(Arrays.asList(expectedUsers)));
    assertEquals("baseurl" , dto.getBaseUrl());
    assertTrue(dto.isForceBaseUrl());
    assertEquals(1 , dto.getLoginAttemptLimit());
    assertTrue("proxyExcludes", dto.getProxyExcludes().containsAll(Arrays.asList(expectedExcludes)));
    assertTrue(dto.isSkipFailedAuthenticators());
    assertEquals("pluginurl" , dto.getPluginUrl());
    assertEquals(2 , dto.getLoginAttemptLimitTimeout());
    assertTrue(dto.isEnabledXsrfProtection());

    assertEquals(expectedBaseUri.toString(), dto.getLinks().getLinkBy("self").get().getHref());
    assertEquals(expectedBaseUri.toString(), dto.getLinks().getLinkBy("update").get().getHref());
  }

  @Test
  public void shouldMapFieldsWithoutUpdate() {
    ScmConfiguration config = createConfiguration();

    when(subject.hasRole("configuration:write:global")).thenReturn(false);
    ConfigDto dto = mapper.map(config);

    assertEquals("baseurl", dto.getBaseUrl());
    assertEquals(expectedBaseUri.toString(), dto.getLinks().getLinkBy("self").get().getHref());
    assertFalse(dto.getLinks().hasLink("update"));
  }

  private ScmConfiguration createConfiguration() {
    ScmConfiguration config = new ScmConfiguration();
    config.setProxyPassword("heartOfGold");
    config.setProxyPort(1234);
    config.setProxyServer("proxyserver");
    config.setProxyUser("trillian");
    config.setEnableProxy(true);
    config.setRealmDescription("description");
    config.setEnableRepositoryArchive(true);
    config.setDisableGroupingGrid(true);
    config.setDateFormat("dd");
    config.setAnonymousAccessEnabled(true);
    config.setAdminGroups(Sets.newSet(expectedGroups));
    config.setAdminUsers(Sets.newSet(expectedUsers));
    config.setBaseUrl("baseurl");
    config.setForceBaseUrl(true);
    config.setLoginAttemptLimit(1);
    config.setProxyExcludes(Sets.newSet(expectedExcludes));
    config.setSkipFailedAuthenticators(true);
    config.setPluginUrl("pluginurl");
    config.setLoginAttemptLimitTimeout(2);
    config.setEnabledXsrfProtection(true);
    return config;
  }

}
