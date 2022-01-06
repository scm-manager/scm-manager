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
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.repository.Compatibility;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.SvnConfig;

import java.net.URI;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SvnConfigToSvnConfigDtoMapperTest {

  private final URI baseUri = URI.create("http://example.com/base/");

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ScmPathInfoStore scmPathInfoStore;

  @Mock
  private RepositoryManager manager;

  @InjectMocks
  private SvnConfigToSvnConfigDtoMapperImpl mapper;

  private final Subject subject = mock(Subject.class);
  private final ThreadState subjectThreadState = new SubjectThreadState(subject);

  private URI expectedBaseUri;

  @Before
  public void init() {
    when(scmPathInfoStore.get().getApiRestUri()).thenReturn(baseUri);
    expectedBaseUri = baseUri.resolve(SvnConfigResource.SVN_CONFIG_PATH_V2);
    subjectThreadState.bind();
    ThreadContext.bind(subject);
  }

  @After
  public void unbindSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  public void shouldMapFields() {
    SvnConfig config = createConfiguration();

    when(manager.getAll()).thenReturn(Collections.singleton(RepositoryTestData.create42Puzzle("svn")));
    when(subject.isPermitted("configuration:write:svn")).thenReturn(true);
    SvnConfigDto dto = mapper.map(config);

    assertTrue(dto.isDisabled());
    assertFalse(dto.isAllowDisable());

    assertEquals(Compatibility.PRE15, dto.getCompatibility());
    assertTrue(dto.isEnabledGZip());

    assertEquals(expectedBaseUri.toString(), dto.getLinks().getLinkBy("self").get().getHref());
    assertEquals(expectedBaseUri.toString(), dto.getLinks().getLinkBy("update").get().getHref());
  }

  @Test
  public void shouldAllowDisableIfNoSubversionRepositoriesExist() {
    SvnConfig config = createConfiguration();

    when(manager.getAll()).thenReturn(Collections.singleton(RepositoryTestData.create42Puzzle("git")));
    when(subject.isPermitted("configuration:write:svn")).thenReturn(true);
    SvnConfigDto dto = mapper.map(config);

    assertTrue(dto.isAllowDisable());
  }

  @Test
  public void shouldMapFieldsWithoutUpdate() {
    SvnConfig config = createConfiguration();

    when(subject.isPermitted("configuration:write:svn")).thenReturn(false);
    SvnConfigDto dto = mapper.map(config);

    assertEquals(expectedBaseUri.toString(), dto.getLinks().getLinkBy("self").get().getHref());
    assertFalse(dto.getLinks().hasLink("update"));
  }

  private SvnConfig createConfiguration() {
    SvnConfig config = new SvnConfig();
    config.setDisabled(true);

    config.setCompatibility(Compatibility.PRE15);
    config.setEnabledGZip(true);

    return config;
  }

}
