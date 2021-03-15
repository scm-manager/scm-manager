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

package sonia.scm.repository.hooks;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class HookServerTest {

  private static final Logger LOG = LoggerFactory.getLogger(HookServerTest.class);

  @BeforeEach
  void setUp() {
    DefaultSecurityManager securityManager = new DefaultSecurityManager();
    ThreadContext.bind(securityManager);
    Subject subject = new Subject.Builder().principals(new SimplePrincipalCollection("Tricia", "Testing")).buildSubject();
    ThreadContext.bind(subject);
  }

  @AfterEach
  void tearDown() {
    ThreadContext.unbindSubject();
    ThreadContext.unbindSecurityManager();
  }

  @Test
  void shouldStartHookServer() throws IOException {
    Response response = send(new Request("Joe"));
    assertThat(response.getGreeting()).isEqualTo("Hello Joe");
    assertThat(response.getGreeter()).isEqualTo("Tricia");
  }

  private Response send(Request request) throws IOException {
    try (HookServer server = new HookServer(HelloHandler::new, new SimpleMeterRegistry())) {
      int port = server.start();
      try (
        Socket socket = new Socket(InetAddress.getLoopbackAddress(), port);
        InputStream input = socket.getInputStream();
        OutputStream output = socket.getOutputStream()
      ) {
        Sockets.send(output, request);
        return Sockets.receive(input, Response.class);
      } catch (IOException ex) {
        throw new RuntimeException("failed", ex);
      }
    }
  }

  public static class HelloHandler implements HookHandler {

    private final Socket socket;

    private HelloHandler(Socket socket) {
      this.socket = socket;
    }

    @Override
    public void run() {
      try (InputStream input = socket.getInputStream(); OutputStream output = socket.getOutputStream()) {
        Request request = Sockets.receive(input, Request.class);
        Subject subject = SecurityUtils.getSubject();
        Sockets.send(output, new Response("Hello " + request.getName(), subject.getPrincipal().toString()));
      } catch (IOException ex) {
        throw new RuntimeException("failed", ex);
      } finally {
        try {
          socket.close();
        } catch (IOException e) {
          LOG.error("failed to close socket", e);
        }
      }
    }
  }

  @Data
  @AllArgsConstructor
  public static class Request {

    private String name;

  }

  @Data
  @AllArgsConstructor
  public static class Response {

    private String greeting;
    private String greeter;

  }

}
