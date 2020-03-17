/**
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

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.Repository;

import java.net.URI;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.Silent.class)
@SubjectAware(
  configuration = "classpath:sonia/scm/repository/shiro.ini"
)
public class RepositoryPermissionToRepositoryPermissionDtoMapperTest {

  @Rule
  public ShiroRule shiro = new ShiroRule();

  private final URI baseUri = URI.create("http://example.com/base/");

  @SuppressWarnings("unused") // Is injected
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @InjectMocks
  RepositoryPermissionToRepositoryPermissionDtoMapperImpl mapper;

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldMapGroupPermissionCorrectly() {
    Repository repository = getDummyRepository();
    RepositoryPermission permission = new RepositoryPermission("42", asList("read","modify","delete"), true);

    RepositoryPermissionDto repositoryPermissionDto = mapper.map(permission, repository);

    assertThat(repositoryPermissionDto.getLinks().getLinkBy("self").isPresent()).isTrue();
    assertThat(repositoryPermissionDto.getLinks().getLinkBy("self").get().getHref()).contains("@42");
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldMapNonGroupPermissionCorrectly() {
    Repository repository = getDummyRepository();
    RepositoryPermission permission = new RepositoryPermission("42", asList("read","modify","delete"), false);

    RepositoryPermissionDto repositoryPermissionDto = mapper.map(permission, repository);

    assertThat(repositoryPermissionDto.getLinks().getLinkBy("self").isPresent()).isTrue();
    assertThat(repositoryPermissionDto.getLinks().getLinkBy("self").get().getHref()).contains("42");
    assertThat(repositoryPermissionDto.getLinks().getLinkBy("self").get().getHref()).doesNotContain("@");
  }

  private Repository getDummyRepository() {
    return new Repository("repo", "git", "foo", "bar");
  }
}
