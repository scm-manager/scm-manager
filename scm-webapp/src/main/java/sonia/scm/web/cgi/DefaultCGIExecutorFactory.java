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

package sonia.scm.web.cgi;


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


public class DefaultCGIExecutorFactory implements CGIExecutorFactory, AutoCloseable
{
  @Override
  public void close() {
    executor.shutdown();
  }

  private final ExecutorService executor;

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

  @Override
  public CGIExecutor createExecutor(ScmConfiguration configuration,
    ServletContext context, HttpServletRequest request,
    HttpServletResponse response)
  {
    return new DefaultCGIExecutor(executor, configuration, context, request,
      response);
  }

}
