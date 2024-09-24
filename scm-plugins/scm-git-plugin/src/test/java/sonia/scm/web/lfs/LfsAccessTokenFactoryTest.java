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
