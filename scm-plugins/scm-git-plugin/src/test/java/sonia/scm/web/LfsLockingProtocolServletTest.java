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

package sonia.scm.web;

import com.fasterxml.jackson.databind.JsonNode;
import org.assertj.core.api.Condition;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.FileLock;
import sonia.scm.repository.api.FileLockedException;
import sonia.scm.repository.spi.GitLockStoreFactory.GitLockStore;
import sonia.scm.user.DisplayUser;
import sonia.scm.user.User;
import sonia.scm.user.UserDisplayManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtendWith(ShiroExtension.class)
class LfsLockingProtocolServletTest {

  private static final Repository REPOSITORY = new Repository("23", "git", "hitchhiker", "hog");
  private static final Instant NOW = Instant.ofEpochSecond(-562031958);

  @Mock
  private GitLockStore lockStore;
  @Mock
  private UserDisplayManager userDisplayManager;

  private LfsLockingProtocolServlet servlet;

  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;
  private final CapturingServletOutputStream responseStream = new CapturingServletOutputStream();

  @BeforeEach
  void setUpServlet() throws IOException {
    servlet = new LfsLockingProtocolServlet(REPOSITORY, lockStore, userDisplayManager);
    lenient().when(response.getOutputStream()).thenReturn(responseStream);
  }

  @BeforeEach
  void setUpUserDisplayManager() {
    lenient().when( userDisplayManager.get("dent"))
      .thenReturn(of(DisplayUser.from(new User("dent", "Arthur Dent", "irrelevant"))));
    lenient().when(userDisplayManager.get("trillian"))
      .thenReturn(of(DisplayUser.from(new User("trillian", "Tricia McMillan", "irrelevant"))));
  }

  @Nested
  class WithValidLocksPath {

    @BeforeEach
    void mockValidPath() {
      when(request.getPathInfo()).thenReturn("repo/hitchhiker/hog.git/info/lfs/locks");
    }

    @Test
    void shouldNotBeAuthorizedToReadLocks() throws IOException {
      servlet.doGet(request, response);

      verify(response).setStatus(403);
      verify(lockStore, never()).getAll();
    }

    @Nested
    @SubjectAware(value = "trillian", permissions = "repository:read,pull:23")
    class WithReadPermission {

      @Test
      void shouldGetEmptyArrayForNoFileLocks() throws IOException {
        servlet.doGet(request, response);

        verify(response).setStatus(200);
        assertThat(responseStream).hasToString("{\"locks\":[]}");
      }

      @Test
      void shouldGetAllExistingFileLocks() throws IOException {
        when(lockStore.getAll())
          .thenReturn(
            asList(
              new FileLock("some/file", "42", "dent", NOW),
              new FileLock("other/file", "1337", "trillian", NOW.plus(42, DAYS))
            ));

        servlet.doGet(request, response);

        verify(response).setStatus(200);
        JsonNode locks = responseStream.getContentAsJson().get("locks");
        assertThat(locks.get(0))
          .is(lockNodeWith("42", "some/file", "Arthur Dent", "1952-03-11T00:00:42Z"));
        assertThat(locks.get(1))
          .is(lockNodeWith("1337", "other/file", "Tricia McMillan", "1952-04-22T00:00:42Z"));
      }

      @Test
      void shouldUseUserIdIfUserIsUnknown() throws IOException {
        when(lockStore.getAll())
          .thenReturn(
            singletonList(
              new FileLock("some/file", "42", "marvin", NOW)
            ));

        servlet.doGet(request, response);

        JsonNode locks = responseStream.getContentAsJson().get("locks");
        assertThat(locks.get(0))
          .is(lockNodeWith("42", "some/file", "marvin", "1952-03-11T00:00:42Z"));
      }

      @Test
      void shouldNotBeAuthorizedToCreateNewLock() throws IOException {
        servlet.doPost(request, response);

        verify(response).setStatus(403);
        verify(lockStore, never()).put(any(), anyBoolean());
      }
    }

    @Nested
    @SubjectAware(value = "trillian", permissions = "repository:read,write,pull,push:23")
    class WithWritePermission {

      @Test
      void shouldCreateNewLock() throws IOException {
        when(request.getInputStream()).thenReturn(new BufferedServletInputStream("{\n" +
          "  \"path\": \"some/file.txt\"\n" +
          "}"));
        when(lockStore.put("some/file.txt", false))
          .thenReturn(new FileLock("some/file.txt", "42", "Tricia", NOW));

        servlet.doPost(request, response);

        verify(response).setStatus(201);
        assertThat(responseStream.getContentAsJson().get("lock"))
          .is(lockNodeWith("42", "some/file.txt", "Tricia", "1952-03-11T00:00:42Z"));
      }

      @Test
      void shouldFailToCreateExistingLock() throws IOException {
        when(request.getInputStream()).thenReturn(new BufferedServletInputStream("{\n" +
          "  \"path\": \"some/file.txt\"\n" +
          "}"));
        when(lockStore.put("some/file.txt", false))
          .thenThrow(new FileLockedException(REPOSITORY.getNamespaceAndName(), new FileLock("some/file.txt", "42", "Tricia", NOW)));

        servlet.doPost(request, response);

        verify(response).setStatus(409);
        JsonNode contentAsJson = responseStream.getContentAsJson();
        assertThat(contentAsJson.get("lock"))
          .is(lockNodeWith("42", "some/file.txt", "Tricia", "1952-03-11T00:00:42Z"));
        assertThat(contentAsJson.get("message")).isNotNull();
      }
    }
  }

  @Test
  void shouldFailForIllegalPath() throws IOException {
    when(request.getPathInfo()).thenReturn("repo/hitchhiker/hog.git/info/lfs/other");

    servlet.doGet(request, response);

    verify(response).setStatus(400);
  }

  private Condition<? super Iterable<? extends JsonNode>> lockNodeWith(String expectedId, String expectedPath, String expectedName, String expectedTimestamp) {
    return new Condition<Iterable<? extends JsonNode>>() {
      @Override
      public boolean matches(Iterable<? extends JsonNode> value) {
        JsonNode node = (JsonNode) value;
        assertThat(node.get("id").asText()).isEqualTo(expectedId);
        assertThat(node.get("path").asText()).isEqualTo(expectedPath);
        assertThat(node.get("owner").get("name").asText()).isEqualTo(expectedName);
        assertThat(node.get("locked_at").asText()).isEqualTo(expectedTimestamp);
        return true;
      }
    };
  }
}
