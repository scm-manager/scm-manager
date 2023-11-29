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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import sonia.scm.repository.spi.GitFileLockStoreFactory.GitFileLockStore;
import sonia.scm.user.DisplayUser;
import sonia.scm.user.User;
import sonia.scm.user.UserDisplayManager;

import java.io.IOException;
import java.time.Instant;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
  private GitFileLockStore lockStore;
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
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    mapper.configure(SerializationFeature.WRITE_DATES_WITH_ZONE_ID, true);
    servlet = new LfsLockingProtocolServlet(REPOSITORY, lockStore, userDisplayManager, mapper, 3, 2);
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
    void shouldNotBeAuthorizedToReadLocks() {
      servlet.doGet(request, response);

      verify(response).setStatus(403);
      verify(lockStore, never()).getAll();
    }

    @Nested
    @SubjectAware(value = "trillian", permissions = "repository:read,pull:23")
    class WithReadPermission {

      @Test
      void shouldGetEmptyArrayForNoFileLocks() {
        servlet.doGet(request, response);

        verify(response).setStatus(200);
        JsonNode locks = responseStream.getContentAsJson().get("locks");
        assertThat(locks).isEmpty();
      }

      @Test
      void shouldGetAllExistingFileLocks() {
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
      void shouldGetExistingLockByPath() {
        when(request.getParameter("path")).thenReturn("some/file");
        when(lockStore.getLock("some/file"))
          .thenReturn(of(new FileLock("some/file", "42", "dent", NOW)));

        servlet.doGet(request, response);

        verify(response).setStatus(200);
        JsonNode locks = responseStream.getContentAsJson().get("locks");
        assertThat(locks.get(0))
          .is(lockNodeWith("42", "some/file", "Arthur Dent", "1952-03-11T00:00:42Z"));
      }

      @Test
      void shouldGetEmptyListForNotExistingLockByPath() {
        when(request.getParameter("path")).thenReturn("some/file");
        when(lockStore.getLock("some/file"))
          .thenReturn(empty());

        servlet.doGet(request, response);

        verify(response).setStatus(200);
        JsonNode locks = responseStream.getContentAsJson().get("locks");
        assertThat(locks).isEmpty();
      }

      @Test
      void shouldGetExistingLockById() {
        when(request.getParameter("path")).thenReturn(null);
        when(request.getParameter("id")).thenReturn("42");
        when(lockStore.getById("42"))
          .thenReturn(of(new FileLock("some/file", "42", "dent", NOW)));

        servlet.doGet(request, response);

        verify(response).setStatus(200);
        JsonNode locks = responseStream.getContentAsJson().get("locks");
        assertThat(locks.get(0))
          .is(lockNodeWith("42", "some/file", "Arthur Dent", "1952-03-11T00:00:42Z"));
      }

      @Test
      void shouldGetEmptyListForNotExistingLockById() {
        when(request.getParameter("path")).thenReturn(null);
        when(request.getParameter("id")).thenReturn("42");
        when(lockStore.getById("42"))
          .thenReturn(empty());

        servlet.doGet(request, response);

        verify(response).setStatus(200);
        JsonNode locks = responseStream.getContentAsJson().get("locks");
        assertThat(locks).isEmpty();
      }

      @Test
      void shouldUseUserIdIfUserIsUnknown() {
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
      void shouldNotBeAuthorizedToCreateNewLock() {
        servlet.doPost(request, response);

        verify(response).setStatus(403);
        verify(lockStore, never()).put(any());
      }

      @Nested
      class WithLimiting {

        @BeforeEach
        void mockManyResults() {
          when(lockStore.getAll())
            .thenReturn(
              asList(
                new FileLock("empty/file", "2", "zaphod", NOW),
                new FileLock("some/file", "23", "dent", NOW),
                new FileLock("any/file", "42", "marvin", NOW),
                new FileLock("other/file", "1337", "trillian", NOW.plus(42, DAYS))
              ));
        }

        @Test
        void shouldLimitFileLocksByDefault() {
          servlet.doGet(request, response);

          verify(response).setStatus(200);
          JsonNode contentAsJson = responseStream.getContentAsJson();
          JsonNode locks = contentAsJson.get("locks");
          assertThat(locks).hasSize(3);
          assertThat(locks.get(0).get("id").asText()).isEqualTo("2");
          assertThat(contentAsJson.get("next_cursor").asText()).isEqualTo("3");
        }

        @Test
        void shouldUseLimitFromRequest() {
          lenient().doReturn("2").when(request).getParameter("limit");

          servlet.doGet(request, response);

          verify(response).setStatus(200);
          JsonNode contentAsJson = responseStream.getContentAsJson();
          JsonNode locks = contentAsJson.get("locks");
          assertThat(locks).hasSize(2);
          assertThat(locks.get(0).get("id").asText()).isEqualTo("2");
          assertThat(contentAsJson.get("next_cursor").asText()).isEqualTo("2");
        }

        @Test
        void shouldUseCursorFromRequest() {
          lenient().doReturn("3").when(request).getParameter("cursor");

          servlet.doGet(request, response);

          verify(response).setStatus(200);
          JsonNode contentAsJson = responseStream.getContentAsJson();
          JsonNode locks = contentAsJson.get("locks");
          assertThat(locks).hasSize(1);
          assertThat(locks.get(0).get("id").asText()).isEqualTo("1337");
          assertThat(contentAsJson.get("next_cursor")).isNull();
        }
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
        when(lockStore.put("some/file.txt"))
          .thenReturn(new FileLock("some/file.txt", "42", "Tricia", NOW));

        servlet.doPost(request, response);

        verify(response).setStatus(201);
        assertThat(responseStream.getContentAsJson().get("lock"))
          .is(lockNodeWith("42", "some/file.txt", "Tricia", "1952-03-11T00:00:42Z"));
      }

      @Test
      void shouldIgnoreUnknownAttributed() throws IOException {
        when(request.getInputStream()).thenReturn(new BufferedServletInputStream("{\n" +
          "  \"path\": \"some/file.txt\",\n" +
          "  \"unknown\": \"attribute\"\n" +
          "}"));
        when(lockStore.put("some/file.txt"))
          .thenReturn(new FileLock("some/file.txt", "42", "Tricia", NOW));

        servlet.doPost(request, response);

        verify(response).setStatus(201);
        assertThat(responseStream.getContentAsJson().get("lock"))
          .is(lockNodeWith("42", "some/file.txt", "Tricia", "1952-03-11T00:00:42Z"));
      }

      @Test
      void shouldHandleInvalidInput() throws IOException {
        when(request.getInputStream()).thenReturn(new BufferedServletInputStream("{\n" +
          "  \"invalidAttribute\": \"some value\"\n" +
          "}"));

        servlet.doPost(request, response);

        verify(response).setStatus(400);
        verify(lockStore, never()).put(any());
      }

      @Test
      void shouldFailToCreateExistingLock() throws IOException {
        when(request.getInputStream()).thenReturn(new BufferedServletInputStream("{\n" +
          "  \"path\": \"some/file.txt\"\n" +
          "}"));
        when(lockStore.put("some/file.txt"))
          .thenThrow(new FileLockedException(REPOSITORY.getNamespaceAndName(), new FileLock("some/file.txt", "42", "Tricia", NOW)));

        servlet.doPost(request, response);

        verify(response).setStatus(409);
        JsonNode contentAsJson = responseStream.getContentAsJson();
        assertThat(contentAsJson.get("lock"))
          .is(lockNodeWith("42", "some/file.txt", "Tricia", "1952-03-11T00:00:42Z"));
        assertThat(contentAsJson.get("message")).isNotNull();
      }

      @Test
      void shouldGetVerifyResult() throws IOException {
        when(request.getPathInfo()).thenReturn("repo/hitchhiker/hog.git/info/lfs/locks/verify");
        when(request.getInputStream()).thenReturn(new BufferedServletInputStream("{}"));
        when(lockStore.getAll())
          .thenReturn(
            asList(
              new FileLock("some/file", "42", "dent", NOW),
              new FileLock("other/file", "1337", "trillian", NOW.plus(42, DAYS))
            ));

        servlet.doPost(request, response);

        verify(response).setStatus(200);
        JsonNode ourLocks = responseStream.getContentAsJson().get("ours");
        assertThat(ourLocks.get(0))
          .is(lockNodeWith("1337", "other/file", "Tricia McMillan", "1952-04-22T00:00:42Z"));
        JsonNode theirLocks = responseStream.getContentAsJson().get("theirs");
        assertThat(theirLocks.get(0))
          .is(lockNodeWith("42", "some/file", "Arthur Dent", "1952-03-11T00:00:42Z"));
      }

      @Test
      void shouldGetVerifyResultForNoFileLocks() throws IOException {
        when(request.getPathInfo()).thenReturn("repo/hitchhiker/hog.git/info/lfs/locks/verify");
        when(request.getInputStream()).thenReturn(new BufferedServletInputStream("{}"));

        servlet.doPost(request, response);

        verify(response).setStatus(200);
        JsonNode ourLocks = responseStream.getContentAsJson().get("ours");
        assertThat(ourLocks).isEmpty();
        JsonNode theirLocks = responseStream.getContentAsJson().get("theirs");
        assertThat(theirLocks).isEmpty();
      }

      @Nested
      class VerifyWithLimiting {

        @BeforeEach
        void mockManyResults() {
          when(lockStore.getAll())
            .thenReturn(
              asList(
                new FileLock("empty/file", "2", "zaphod", NOW),
                new FileLock("some/file", "23", "dent", NOW),
                new FileLock("any/file", "42", "marvin", NOW),
                new FileLock("other/file", "1337", "trillian", NOW.plus(42, DAYS))
              ));
        }

        @Test
        void shouldLimitVerifyByDefault() throws IOException {
          when(request.getPathInfo()).thenReturn("repo/hitchhiker/hog.git/info/lfs/locks/verify");
          when(request.getInputStream()).thenReturn(new BufferedServletInputStream("{}"));

          servlet.doPost(request, response);

          verify(response).setStatus(200);
          JsonNode contentAsJson = responseStream.getContentAsJson();
          JsonNode ourLocks = contentAsJson.get("ours");
          assertThat(ourLocks).isEmpty();
          JsonNode theirLocks = contentAsJson.get("theirs");
          assertThat(theirLocks).hasSize(3);
          assertThat(theirLocks.get(0).get("id").asText()).isEqualTo("2");
          assertThat(contentAsJson.get("next_cursor").asText()).isEqualTo("3");
        }

        @Test
        void shouldUseLimitFromRequest() throws IOException {
          when(request.getPathInfo()).thenReturn("repo/hitchhiker/hog.git/info/lfs/locks/verify");
          when(request.getInputStream()).thenReturn(new BufferedServletInputStream("{\"limit\":2}"));

          servlet.doPost(request, response);

          verify(response).setStatus(200);
          JsonNode contentAsJson = responseStream.getContentAsJson();
          JsonNode ourLocks = contentAsJson.get("ours");
          assertThat(ourLocks).isEmpty();
          JsonNode theirLocks = contentAsJson.get("theirs");
          assertThat(theirLocks).hasSize(2);
          assertThat(theirLocks.get(0).get("id").asText()).isEqualTo("2");
          assertThat(contentAsJson.get("next_cursor").asText()).isEqualTo("2");
        }

        @Test
        void shouldUseCursorFromRequest() throws IOException {
          when(request.getPathInfo()).thenReturn("repo/hitchhiker/hog.git/info/lfs/locks/verify");
          when(request.getInputStream()).thenReturn(new BufferedServletInputStream("{\"cursor\":\"3\"}"));

          servlet.doPost(request, response);

          verify(response).setStatus(200);
          JsonNode contentAsJson = responseStream.getContentAsJson();
          JsonNode ourLocks = contentAsJson.get("ours");
          assertThat(ourLocks).hasSize(1);
          assertThat(ourLocks.get(0).get("id").asText()).isEqualTo("1337");
          JsonNode theirLocks = contentAsJson.get("theirs");
          assertThat(theirLocks).isEmpty();
          assertThat(contentAsJson.get("next_cursor")).isNull();
        }
      }

      @Test
      void shouldDeleteExistingFileLock() throws IOException {
        when(request.getPathInfo()).thenReturn("repo/hitchhiker/hog.git/info/lfs/locks/42/unlock");
        when(request.getInputStream()).thenReturn(new BufferedServletInputStream("{}"));
        FileLock expectedLock = new FileLock("some/file.txt", "42", "trillian", NOW);
        when(lockStore.removeById("42", false))
          .thenReturn(of(expectedLock));

        servlet.doPost(request, response);

        verify(response).setStatus(200);
        JsonNode deletedLock = responseStream.getContentAsJson().get("lock");
        assertThat(deletedLock).is(lockNodeWith(expectedLock, "Tricia McMillan"));
      }

      @Test
      void shouldFailToDeleteFileLockByAnotherUser() throws IOException {
        when(request.getPathInfo()).thenReturn("repo/hitchhiker/hog.git/info/lfs/locks/42/unlock");
        when(request.getInputStream()).thenReturn(new BufferedServletInputStream("{}"));
        when(lockStore.removeById("42", false))
          .thenThrow(new FileLockedException(REPOSITORY.getNamespaceAndName(), new FileLock("some/file.txt", "42", "dent", NOW)));

        servlet.doPost(request, response);

        verify(response).setStatus(403);
      }

      @Test
      void shouldDeleteExistingLockWithForceFlag() throws IOException {
        when(request.getPathInfo()).thenReturn("repo/hitchhiker/hog.git/info/lfs/locks/42/unlock");
        when(request.getInputStream()).thenReturn(new BufferedServletInputStream("{\"force\":true}"));
        FileLock expectedLock = new FileLock("some/file.txt", "42", "dent", NOW);
        when(lockStore.removeById("42", true))
          .thenReturn(of(expectedLock));

        servlet.doPost(request, response);

        verify(response).setStatus(200);
        JsonNode deletedLock = responseStream.getContentAsJson().get("lock");
        assertThat(deletedLock).is(lockNodeWith(expectedLock, "Arthur Dent"));
      }
    }
  }

  @Test
  void shouldFailForIllegalPath() {
    when(request.getPathInfo()).thenReturn("repo/hitchhiker/hog.git/info/lfs/other");

    servlet.doGet(request, response);

    verify(response).setStatus(400);
  }

  private Condition<? super Iterable<? extends JsonNode>> lockNodeWith(FileLock lock, String expectedName) {
    return new Condition<Iterable<? extends JsonNode>>() {
      @Override
      public boolean matches(Iterable<? extends JsonNode> value) {
        JsonNode node = (JsonNode) value;
        assertThat(node.get("id").asText()).isEqualTo(lock.getId());
        assertThat(node.get("path").asText()).isEqualTo(lock.getPath());
        assertThat(node.get("owner").get("name").asText()).isEqualTo(expectedName);
        assertThat(node.get("locked_at").asText()).isEqualTo(lock.getTimestamp().toString());
        return true;
      }
    };
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
