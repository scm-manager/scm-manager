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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.repository.RepositoryType;
import sonia.scm.repository.api.Command;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class RepositoryTypeToRepositoryTypeDtoMapperTest {

  private final URI baseUri = URI.create("https://scm-manager.org/scm/");

  @SuppressWarnings("unused") // Is injected
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @Mock
  private Subject subject;

  @InjectMocks
  private RepositoryTypeToRepositoryTypeDtoMapperImpl mapper;

  private final RepositoryType type = new RepositoryType("hk", "Hitchhiker", Sets.newHashSet());

  @Before
  public void init() {
    ThreadContext.bind(subject);
  }

  @After
  public void tearDown() {
    ThreadContext.unbindSubject();
  }

  @Test
  public void shouldMapSimpleProperties() {
    RepositoryTypeDto dto = mapper.map(type);
    assertEquals("hk", dto.getName());
    assertEquals("Hitchhiker", dto.getDisplayName());
  }

  @Test
  public void shouldAppendSelfLink() {
    RepositoryTypeDto dto = mapper.map(type);
    assertEquals(
      "https://scm-manager.org/scm/v2/repositoryTypes/hk",
      dto.getLinks().getLinkBy("self").get().getHref()
    );
  }

  @Test
  public void shouldAppendImportFromUrlLink() {
    RepositoryType type = new RepositoryType("hk", "Hitchhiker", ImmutableSet.of(Command.PULL));
    when(subject.isPermitted("repository:create")).thenReturn(true);

    RepositoryTypeDto dto = mapper.map(type);
    assertEquals(
      "https://scm-manager.org/scm/v2/repositories/import/hk/url",
      dto.getLinks().getLinkBy("importFromUrl").get().getHref()
    );
  }

  @Test
  public void shouldNotAppendImportFromUrlLinkIfCommandNotSupported() {
    when(subject.isPermitted("repository:create")).thenReturn(true);
    RepositoryTypeDto dto = mapper.map(type);
    assertFalse(dto.getLinks().getLinkBy("importFromUrl").isPresent());
  }

  @Test
  public void shouldNotAppendImportFromUrlLinkIfNotPermitted() {
    RepositoryType type = new RepositoryType("hk", "Hitchhiker", ImmutableSet.of(Command.PULL));

    RepositoryTypeDto dto = mapper.map(type);
    assertFalse(dto.getLinks().getLinkBy("importFromUrl").isPresent());
  }
}
