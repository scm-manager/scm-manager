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

public class GlobalConfigDtoToScmConfigurationMapperTest {

  @InjectMocks
  private GlobalConfigDtoToScmConfigurationMapperImpl mapper;

  private String[] expectedUsers = { "trillian", "arthur" };
  private String[] expectedGroups = { "admin", "plebs" };
  private String[] expectedExcludes = { "ex", "clude" };

  @Before
  public void init() {
    initMocks(this);
  }

  @Test
  public void shouldMapFields() {
    GlobalConfigDto dto = createDefaultDto();
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

  private GlobalConfigDto createDefaultDto() {
    GlobalConfigDto globalConfigDto = new GlobalConfigDto();
    globalConfigDto.setProxyPassword("prPw");
    globalConfigDto.setProxyPort(42);
    globalConfigDto.setProxyServer("srvr");
    globalConfigDto.setProxyUser("user");
    globalConfigDto.setEnableProxy(true);
    globalConfigDto.setRealmDescription("realm");
    globalConfigDto.setEnableRepositoryArchive(true);
    globalConfigDto.setDisableGroupingGrid(true);
    globalConfigDto.setDateFormat("yyyy");
    globalConfigDto.setAnonymousAccessEnabled(true);
    globalConfigDto.setAdminGroups(Sets.newSet(expectedGroups));
    globalConfigDto.setAdminUsers(Sets.newSet(expectedUsers));
    globalConfigDto.setBaseUrl("baseurl");
    globalConfigDto.setForceBaseUrl(true);
    globalConfigDto.setLoginAttemptLimit(41);
    globalConfigDto.setProxyExcludes(Sets.newSet(expectedExcludes));
    globalConfigDto.setSkipFailedAuthenticators(true);
    globalConfigDto.setPluginUrl("https://plug.ins");
    globalConfigDto.setLoginAttemptLimitTimeout(40);
    globalConfigDto.setEnabledXsrfProtection(true);

    return globalConfigDto;
  }
}
