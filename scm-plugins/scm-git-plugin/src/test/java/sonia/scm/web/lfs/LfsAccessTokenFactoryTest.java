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

package sonia.scm.web.lfs;

import org.apache.shiro.authz.UnauthorizedException;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.GitConfig;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.security.AccessToken;
import sonia.scm.security.AccessTokenBuilder;
import sonia.scm.security.AccessTokenBuilderFactory;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtendWith(ShiroExtension.class)
class LfsAccessTokenFactoryTest {

  @Mock
  private AccessTokenBuilderFactory tokenBuilderFactory;
  @Mock(answer = Answers.RETURNS_SELF)
  private AccessTokenBuilder tokenBuilder;
  @Mock
  private GitRepositoryHandler handler;
  @InjectMocks
  private LfsAccessTokenFactory factory;

  @Mock
  private AccessToken createdTokenFromMock;

  private Repository repository = RepositoryTestData.createHeartOfGold();

  @BeforeEach
  void initRepository() {
    repository.setId("42");
  }

  @Nested
  class WithPermissions {
    @BeforeEach
    void initTokenBuilder() {
      when(tokenBuilderFactory.create()).thenReturn(tokenBuilder);
      when(tokenBuilder.build()).thenReturn(createdTokenFromMock);
    }

    @Test
    @SubjectAware(
      value = "trillian",
      permissions = "repository:read,pull:42"
    )
    void shouldCreateReadToken() {
      AccessToken readAccessToken = factory.createReadAccessToken(repository);

      assertThat(readAccessToken).isSameAs(createdTokenFromMock);

      verify(tokenBuilder).expiresIn(5, TimeUnit.MINUTES);
      verify(tokenBuilder).scope(argThat(scope -> {
        assertThat(scope.iterator()).toIterable()
          .containsExactly("repository:read:42", "repository:pull:42");
        return true;
      }));
    }

    @Test
    @SubjectAware(
      value = "trillian",
      permissions = "repository:read,pull,push:42"
    )
    void shouldCreateReadTokenWithPushIfPermitted() {
      AccessToken readAccessToken = factory.createReadAccessToken(repository);

      assertThat(readAccessToken).isSameAs(createdTokenFromMock);

      verify(tokenBuilder).expiresIn(5, TimeUnit.MINUTES);
      verify(tokenBuilder).scope(argThat(scope -> {
        assertThat(scope.iterator()).toIterable()
          .containsExactly("repository:read:42", "repository:pull:42", "repository:push:42");
        return true;
      }));
    }

    @Test
    @SubjectAware(
      value = "trillian",
      permissions = "repository:read,pull,push:42"
    )
    void shouldCreateWriteToken() {
      GitConfig config = new GitConfig();
      config.setLfsWriteAuthorizationExpirationInMinutes(23);
      when(handler.getConfig()).thenReturn(config);

      AccessToken writeAccessToken = factory.createWriteAccessToken(repository);

      assertThat(writeAccessToken).isSameAs(createdTokenFromMock);

      verify(tokenBuilder).expiresIn(23, TimeUnit.MINUTES);
      verify(tokenBuilder).scope(argThat(scope -> {
        assertThat(scope.iterator()).toIterable()
          .containsExactly("repository:read:42", "repository:pull:42", "repository:push:42");
        return true;
      }));
    }
  }

  @Test
  @SubjectAware(
    value = "trillian",
    permissions = "repository:read:42"
  )
  void shouldFailToCreateReadTokenWithoutPullPermission() {
    assertThrows(UnauthorizedException.class, () -> factory.createReadAccessToken(repository));
  }

  @Test
  @SubjectAware(
    value = "trillian",
    permissions = "repository:pull:42"
  )
  void shouldFailToCreateReadTokenWithoutReadPermission() {
    assertThrows(UnauthorizedException.class, () -> factory.createReadAccessToken(repository));
  }

  @Test
  @SubjectAware(
    value = "trillian",
    permissions = "repository:pull,push:42"
  )
  void shouldFailToCreateWriteTokenWithoutReadPermission() {
    assertThrows(UnauthorizedException.class, () -> factory.createWriteAccessToken(repository));
  }

  @Test
  @SubjectAware(
    value = "trillian",
    permissions = "repository:read,push:42"
  )
  void shouldFailToCreateWriteTokenWithoutPullPermission() {
    assertThrows(UnauthorizedException.class, () -> factory.createWriteAccessToken(repository));
  }

  @Test
  @SubjectAware(
    value = "trillian",
    permissions = "repository:read,pull:42"
  )
  void shouldFailToCreateWriteTokenWithoutPushPermission() {
    assertThrows(UnauthorizedException.class, () -> factory.createWriteAccessToken(repository));
  }
}
