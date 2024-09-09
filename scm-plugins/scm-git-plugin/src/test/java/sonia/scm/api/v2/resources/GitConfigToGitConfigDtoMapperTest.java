/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
import sonia.scm.repository.GitConfig;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryTestData;

import java.net.URI;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GitConfigToGitConfigDtoMapperTest {

  private URI baseUri = URI.create("http://example.com/base/");

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ScmPathInfoStore scmPathInfoStore;

  @Mock
  private RepositoryManager repositoryManager;

  @InjectMocks
  private GitConfigToGitConfigDtoMapperImpl mapper;

  private final Subject subject = mock(Subject.class);
  private final ThreadState subjectThreadState = new SubjectThreadState(subject);

  private URI expectedBaseUri;

  @Before
  public void init() {
    when(scmPathInfoStore.get().getApiRestUri()).thenReturn(baseUri);
    expectedBaseUri = baseUri.resolve(GitConfigResource.GIT_CONFIG_PATH_V2);
    subjectThreadState.bind();
    ThreadContext.bind(subject);
  }

  @After
  public void unbindSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  public void shouldMapFields() {
    GitConfig config = createConfiguration();

    when(repositoryManager.getAll()).thenReturn(Collections.singleton(RepositoryTestData.create42Puzzle("git")));
    when(subject.isPermitted("configuration:write:git")).thenReturn(true);
    GitConfigDto dto = mapper.map(config);

    assertFalse(dto.isAllowDisable());
    assertEquals("express", dto.getGcExpression());
    assertFalse(dto.isDisabled());
    assertEquals(expectedBaseUri.toString(), dto.getLinks().getLinkBy("self").get().getHref());
    assertEquals(expectedBaseUri.toString(), dto.getLinks().getLinkBy("update").get().getHref());
  }

  @Test
  public void shouldAllowDisableIfNoGitRepositoryExist() {
    GitConfig config = createConfiguration();

    when(repositoryManager.getAll()).thenReturn(Collections.singleton(RepositoryTestData.create42Puzzle("hg")));
    when(subject.isPermitted("configuration:write:git")).thenReturn(true);
    GitConfigDto dto = mapper.map(config);

    assertTrue(dto.isAllowDisable());
  }

  @Test
  public void shouldMapFieldsWithoutUpdate() {
    GitConfig config = createConfiguration();

    when(subject.isPermitted("configuration:write:git")).thenReturn(false);
    GitConfigDto dto = mapper.map(config);

    assertEquals(expectedBaseUri.toString(), dto.getLinks().getLinkBy("self").get().getHref());
    assertFalse(dto.getLinks().hasLink("update"));
  }

  private GitConfig createConfiguration() {
    GitConfig config = new GitConfig();
    config.setDisabled(false);
    config.setGcExpression("express");
    return config;
  }

}
