/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.api.v2.resources;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.internal.util.collections.Sets;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.security.AnonymousMode;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

public class ConfigDtoToScmConfigurationMapperTest {

  @InjectMocks
  private ConfigDtoToScmConfigurationMapperImpl mapper;

  private String[] expectedUsers = {"trillian", "arthur"};
  private String[] expectedGroups = {"admin", "plebs"};
  private String[] expectedExcludes = {"ex", "clude"};

  @Before
  public void init() {
    initMocks(this);
  }

  @Test
  public void shouldMapFields() {
    ConfigDto dto = createDefaultDto();
    ScmConfiguration config = mapper.map(dto);

    assertEquals("prPw", config.getProxyPassword());
    assertEquals(42, config.getProxyPort());
    assertEquals("srvr", config.getProxyServer());
    assertEquals("user", config.getProxyUser());
    assertTrue(config.isEnableProxy());
    assertEquals("realm", config.getRealmDescription());
    assertTrue(config.isDisableGroupingGrid());
    assertEquals("yyyy", config.getDateFormat());
    assertEquals(AnonymousMode.PROTOCOL_ONLY, config.getAnonymousMode());
    assertEquals("baseurl", config.getBaseUrl());
    assertTrue(config.isForceBaseUrl());
    assertEquals(41, config.getLoginAttemptLimit());
    assertTrue("proxyExcludes", config.getProxyExcludes().containsAll(Arrays.asList(expectedExcludes)));
    assertTrue(config.isSkipFailedAuthenticators());
    assertEquals("https://plug.ins", config.getPluginUrl());
    assertEquals(40, config.getLoginAttemptLimitTimeout());
    assertTrue(config.isEnabledXsrfProtection());
    assertEquals("username", config.getNamespaceStrategy());
    assertEquals("https://scm-manager.org/login-info", config.getLoginInfoUrl());
  }

  @Test
  public void shouldMapAnonymousAccessFieldToAnonymousMode() {
    ConfigDto dto = createDefaultDto();

    ScmConfiguration config = mapper.map(dto);

    assertEquals(AnonymousMode.PROTOCOL_ONLY, config.getAnonymousMode());

    dto.setAnonymousMode(null);
    dto.setAnonymousAccessEnabled(false);
    ScmConfiguration config2 = mapper.map(dto);

    assertEquals(AnonymousMode.OFF, config2.getAnonymousMode());
  }

  private ConfigDto createDefaultDto() {
    ConfigDto configDto = new ConfigDto();
    configDto.setProxyPassword("prPw");
    configDto.setProxyPort(42);
    configDto.setProxyServer("srvr");
    configDto.setProxyUser("user");
    configDto.setEnableProxy(true);
    configDto.setRealmDescription("realm");
    configDto.setDisableGroupingGrid(true);
    configDto.setDateFormat("yyyy");
    configDto.setAnonymousMode(AnonymousMode.PROTOCOL_ONLY);
    configDto.setBaseUrl("baseurl");
    configDto.setForceBaseUrl(true);
    configDto.setLoginAttemptLimit(41);
    configDto.setProxyExcludes(Sets.newSet(expectedExcludes));
    configDto.setSkipFailedAuthenticators(true);
    configDto.setPluginUrl("https://plug.ins");
    configDto.setLoginAttemptLimitTimeout(40);
    configDto.setEnabledXsrfProtection(true);
    configDto.setNamespaceStrategy("username");
    configDto.setLoginInfoUrl("https://scm-manager.org/login-info");

    return configDto;
  }
}
