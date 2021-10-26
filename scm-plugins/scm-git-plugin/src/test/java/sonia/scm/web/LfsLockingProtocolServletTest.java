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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.api.FileLock;
import sonia.scm.repository.spi.GitLockStoreFactory.GitLockStore;
import sonia.scm.user.DisplayUser;
import sonia.scm.user.User;
import sonia.scm.user.UserDisplayManager;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LfsLockingProtocolServletTest {

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
  private ServletOutputStream responseStream = new CapturingServletOutputStream();

  @BeforeEach
  void setUpServlet() throws IOException {
    servlet = new LfsLockingProtocolServlet(lockStore, userDisplayManager);
    when(response.getOutputStream()).thenReturn(responseStream);
  }

  @BeforeEach
  void setUpUserDisplayManager() {
    lenient().when( userDisplayManager.get("dent"))
      .thenReturn(of(DisplayUser.from(new User("dent", "Arthur Dent", "irrelevant"))));
    lenient().when(userDisplayManager.get("trillian"))
      .thenReturn(of(DisplayUser.from(new User("trillian", "Tricia McMillan", "irrelevant"))));
  }

  @Test
  void shouldGetEmptyArrayForNoFileLocks() throws IOException {
    when(request.getPathInfo()).thenReturn("repo/hitchhiker/hog.git/info/lfs/locks");

    servlet.doGet(request, response);

    assertThat(responseStream).hasToString("{\"locks\":[]}");
  }

  @Test
  void shouldGetAllExistingFileLocks() throws IOException {
    when(request.getPathInfo()).thenReturn("repo/hitchhiker/hog.git/info/lfs/locks");
    when(lockStore.getAll())
      .thenReturn(
        asList(
          new FileLock("some/file", "42", "dent", NOW),
          new FileLock("other/file", "1337", "trillian", NOW.plus(42, DAYS))
        ));

    servlet.doGet(request, response);

    assertThat(responseStream).hasToString(
      "{\"locks\":[" +
        "{\"id\":\"42\",\"path\":\"some/file\",\"owner\":{\"name\":\"Arthur Dent\"},\"locked_at\":\"1952-03-11T00:00:42Z\"}," +
        "{\"id\":\"1337\",\"path\":\"other/file\",\"owner\":{\"name\":\"Tricia McMillan\"},\"locked_at\":\"1952-04-22T00:00:42Z\"}" +
        "]}");
  }

  @Test
  void shouldUseUserIdIfUserIsUnknown() throws IOException {
    when(request.getPathInfo()).thenReturn("repo/hitchhiker/hog.git/info/lfs/locks");
    when(lockStore.getAll())
      .thenReturn(
        singletonList(
          new FileLock("some/file", "42", "marvin", NOW)
        ));

    servlet.doGet(request, response);

    assertThat(responseStream).hasToString(
      "{\"locks\":[" +
        "{\"id\":\"42\",\"path\":\"some/file\",\"owner\":{\"name\":\"marvin\"},\"locked_at\":\"1952-03-11T00:00:42Z\"}" +
        "]}");
  }
}
