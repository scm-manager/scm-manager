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

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.Priority;
import sonia.scm.SCMContext;
import sonia.scm.Stage;
import sonia.scm.event.ScmEventBus;
import sonia.scm.filter.WebElement;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This servlet sends a {@link RestartEvent} to the {@link ScmEventBus} which causes scm-manager to restart the context.
 * The {@link RestartServlet} can be used for reloading java code or for installing plugins without a complete restart.
 * At the moment the Servlet accepts only request, if scm-manager was started in the {@link Stage#DEVELOPMENT} stage.
 *
 * @since 2.0.0
 */
@Priority(0)
@WebElement("/restart")
public class RestartServlet extends HttpServlet {

  private static final Logger LOG = LoggerFactory.getLogger(RestartServlet.class);

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final AtomicBoolean restarting = new AtomicBoolean();

  private final ScmEventBus eventBus;
  private final Stage stage;

  @Inject
  public RestartServlet() {
    this(ScmEventBus.getInstance(), SCMContext.getContext().getStage());
  }

  RestartServlet(ScmEventBus eventBus, Stage stage) {
    this.eventBus = eventBus;
    this.stage = stage;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
    LOG.info("received sendRestartEvent request");

    if (isRestartAllowed()) {

      try (InputStream requestInput = req.getInputStream()) {
        Reason reason = objectMapper.readValue(requestInput, Reason.class);
        sendRestartEvent(resp, reason);
      } catch (IOException ex) {
        LOG.warn("failed to trigger sendRestartEvent event", ex);
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }

    } else {
      LOG.debug("received restart event in non development stage");
      resp.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
    }
  }

  private boolean isRestartAllowed() {
    return stage == Stage.DEVELOPMENT;
  }

  private void sendRestartEvent(HttpServletResponse response, Reason reason) {
    if ( restarting.compareAndSet(false, true) ) {
      LOG.info("trigger sendRestartEvent, because of {}", reason.getMessage());
      eventBus.post(new RestartEvent(RestartServlet.class, reason.getMessage()));

      response.setStatus(HttpServletResponse.SC_ACCEPTED);
    } else {
      LOG.warn("scm-manager restarts already");
      response.setStatus(HttpServletResponse.SC_CONFLICT);
    }
  }

  public static class Reason {

    private String message;

    public void setMessage(String message) {
      this.message = message;
    }

    public String getMessage() {
      return message;
    }
  }
}
