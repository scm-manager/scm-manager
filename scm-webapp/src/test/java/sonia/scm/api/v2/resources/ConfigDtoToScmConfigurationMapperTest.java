package sonia.scm.api.v2.resources;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.internal.util.collections.Sets;
import sonia.scm.config.ScmConfiguration;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

public class ConfigDtoToScmConfigurationMapperTest {

  @InjectMocks
  private ConfigDtoToScmConfigurationMapperImpl mapper;

  private String[] expectedUsers = { "trillian", "arthur" };
  private String[] expectedGroups = { "admin", "plebs" };
  private String[] expectedExcludes = { "ex", "clude" };

  @Before
  public void init() {
    initMocks(this);
  }

  @Test
  public void shouldMapFields() {
    ConfigDto dto = createDefaultDto();
    ScmConfiguration config = mapper.map(dto);

    assertEquals("prPw" , config.getProxyPassword());
    assertEquals(42 , config.getProxyPort());
    assertEquals("srvr" , config.getProxyServer());
    assertEquals("user" , config.getProxyUser());
    assertTrue(config.isEnableProxy());
    assertEquals("realm" , config.getRealmDescription());
    assertTrue(config.isEnableRepositoryArchive());
    assertTrue(config.isDisableGroupingGrid());
    assertEquals("yyyy" , config.getDateFormat());
    assertTrue(config.isAnonymousAccessEnabled());
    assertTrue("adminGroups", config.getAdminGroups().containsAll(Arrays.asList(expectedGroups)));
    assertTrue("adminUsers", config.getAdminUsers().containsAll(Arrays.asList(expectedUsers)));
    assertEquals("baseurl" , config.getBaseUrl());
    assertTrue(config.isForceBaseUrl());
    assertEquals(41 , config.getLoginAttemptLimit());
    assertTrue("proxyExcludes", config.getProxyExcludes().containsAll(Arrays.asList(expectedExcludes)));
    assertTrue(config.isSkipFailedAuthenticators());
    assertEquals("https://plug.ins" , config.getPluginUrl());
    assertEquals(40 , config.getLoginAttemptLimitTimeout());
    assertTrue(config.isEnabledXsrfProtection());
  }

  private ConfigDto createDefaultDto() {
    ConfigDto configDto = new ConfigDto();
    configDto.setProxyPassword("prPw");
    configDto.setProxyPort(42);
    configDto.setProxyServer("srvr");
    configDto.setProxyUser("user");
    configDto.setEnableProxy(true);
    configDto.setRealmDescription("realm");
    configDto.setEnableRepositoryArchive(true);
    configDto.setDisableGroupingGrid(true);
    configDto.setDateFormat("yyyy");
    configDto.setAnonymousAccessEnabled(true);
    configDto.setAdminGroups(Sets.newSet(expectedGroups));
    configDto.setAdminUsers(Sets.newSet(expectedUsers));
    configDto.setBaseUrl("baseurl");
    configDto.setForceBaseUrl(true);
    configDto.setLoginAttemptLimit(41);
    configDto.setProxyExcludes(Sets.newSet(expectedExcludes));
    configDto.setSkipFailedAuthenticators(true);
    configDto.setPluginUrl("https://plug.ins");
    configDto.setLoginAttemptLimitTimeout(40);
    configDto.setEnabledXsrfProtection(true);

    return configDto;
  }
}
