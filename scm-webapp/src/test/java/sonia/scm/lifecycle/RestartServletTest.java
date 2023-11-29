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

package sonia.scm.lifecycle;

import com.github.legman.Subscribe;
import com.google.common.base.Charsets;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.Stage;
import sonia.scm.event.ScmEventBus;
import sonia.scm.event.ScmTestEventBus;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RestartServletTest {

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  private RestartServlet restartServlet;

  private EventListener listener;

  private void setUpObjectUnderTest(Stage stage) {
    listener = new EventListener();
    ScmEventBus eventBus = ScmTestEventBus.getInstance();
    eventBus.register(listener);

    restartServlet = new RestartServlet(eventBus, stage);
  }

  @Test
  public void testRestart() throws IOException {
    setUpObjectUnderTest(Stage.DEVELOPMENT);
    setRequestInputReason("something changed");

    restartServlet.doPost(request, response);

    verify(response).setStatus(HttpServletResponse.SC_ACCEPTED);

    RestartEvent restartEvent = listener.restartEvent;
    assertThat(restartEvent).isNotNull();
    assertThat(restartEvent.getCause()).isEqualTo(RestartServlet.class);
    assertThat(restartEvent.getReason()).isEqualTo("something changed");
  }

  @Test
  public void testRestartCalledTwice() throws IOException {
    setUpObjectUnderTest(Stage.DEVELOPMENT);

    setRequestInputReason("initial change");
    restartServlet.doPost(request, response);
    verify(response).setStatus(HttpServletResponse.SC_ACCEPTED);

    setRequestInputReason("changed again");
    restartServlet.doPost(request, response);
    verify(response).setStatus(HttpServletResponse.SC_CONFLICT);
  }

  @Test
  public void testRestartWithInvalidContent() throws IOException {
    setUpObjectUnderTest(Stage.DEVELOPMENT);

    setRequestInputContent("invalid json");

    restartServlet.doPost(request, response);

    verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
  }

  @Test
  public void testRestartInProductionStage() throws IOException {
    setUpObjectUnderTest(Stage.PRODUCTION);

    setRequestInputReason("initial change");

    restartServlet.doPost(request, response);

    verify(response).setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
  }

  private void setRequestInputReason(String message) throws IOException {
    String content = createReason(message);
    setRequestInputContent(content);
  }

  private void setRequestInputContent(String content) throws IOException {
    InputStream input = createReasonAsInputStream(content);
    when(request.getInputStream()).thenReturn(createServletInputStream(input));
  }

  private ServletInputStream createServletInputStream(final InputStream inputStream) {
    return new ServletInputStream() {
      @Override
      public boolean isFinished() {
        return false;
      }

      @Override
      public boolean isReady() {
        return false;
      }

      @Override
      public void setReadListener(ReadListener readListener) {

      }

      @Override
      public int read() throws IOException {
        return inputStream.read();
      }
    };
  }

  private InputStream createReasonAsInputStream(String content) {
    return new ByteArrayInputStream(content.getBytes(Charsets.UTF_8));
  }

  private String createReason(String message) {
    return String.format("{\"message\": \"%s\"}", message);
  }

  public static class EventListener {

    private RestartEvent restartEvent;

    @Subscribe(async = false)
    public void store(RestartEvent restartEvent) {
      this.restartEvent = restartEvent;
    }

  }
}
