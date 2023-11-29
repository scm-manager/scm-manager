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

package sonia.scm.web.cgi;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.metrics.Metrics;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public class DefaultCGIExecutorFactory implements CGIExecutorFactory, AutoCloseable
{

  /**
   * Constructs ...
   *
   */
  @Inject
  public DefaultCGIExecutorFactory(MeterRegistry registry) {
    this.executor = createExecutor(registry);
  }

  private ExecutorService createExecutor(MeterRegistry registry) {
    ExecutorService executorService = Executors.newCachedThreadPool(
      new ThreadFactoryBuilder()
        .setNameFormat("cgi-pool-%d")
        .build()
    );
    Metrics.executor(registry, executorService, "CGI", "cached");
    return executorService;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param configuration
   * @param context
   * @param request
   * @param response
   *
   * @return
   */
  @Override
  public CGIExecutor createExecutor(ScmConfiguration configuration,
    ServletContext context, HttpServletRequest request,
    HttpServletResponse response)
  {
    return new DefaultCGIExecutor(executor, configuration, context, request,
      response);
  }

  //~--- fields ---------------------------------------------------------------

  @Override
  public void close() {
    executor.shutdown();
  }

  /** Field description */
  private final ExecutorService executor;
}
