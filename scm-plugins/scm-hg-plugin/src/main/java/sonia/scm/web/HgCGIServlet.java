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

package sonia.scm.web;

import com.google.common.base.Stopwatch;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.annotation.Nonnull;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContext;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.HgConfig;
import sonia.scm.repository.HgConfigResolver;
import sonia.scm.repository.HgEnvironmentBuilder;
import sonia.scm.repository.HgExtensions;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.RepositoryRequestListenerUtil;
import sonia.scm.repository.spi.ScmProviderHttpServlet;
import sonia.scm.web.cgi.CGIExecutor;
import sonia.scm.web.cgi.CGIExecutorFactory;
import sonia.scm.web.cgi.EnvList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Singleton
public class HgCGIServlet extends HttpServlet implements ScmProviderHttpServlet {

  private static final long serialVersionUID = -3492811300905099810L;

  
  private static final Logger logger =
    LoggerFactory.getLogger(HgCGIServlet.class);

  private final CGIExecutorFactory cgiExecutorFactory;
  private final HgConfigResolver configResolver;
  private final File extension;
  private final ScmConfiguration configuration;
  private final HgCGIExceptionHandler exceptionHandler;
  private final RepositoryRequestListenerUtil requestListenerUtil;
  private final HgEnvironmentBuilder environmentBuilder;

  @Inject
  public HgCGIServlet(CGIExecutorFactory cgiExecutorFactory,
                      HgConfigResolver configResolver,
                      ScmConfiguration configuration,
                      RepositoryRequestListenerUtil requestListenerUtil,
                      HgEnvironmentBuilder environmentBuilder)
  {
    this.cgiExecutorFactory = cgiExecutorFactory;
    this.configResolver = configResolver;
    this.configuration = configuration;
    this.requestListenerUtil = requestListenerUtil;
    this.environmentBuilder = environmentBuilder;
    this.exceptionHandler = new HgCGIExceptionHandler();
    this.extension = HgExtensions.CGISERVE.getFile(SCMContext.getContext());
  }

  @Override
  public void service(HttpServletRequest request,
    HttpServletResponse response, Repository repository)
  {
    if (!configResolver.isConfigured())
    {
      exceptionHandler.sendFormattedError(request, response,
        HgCGIExceptionHandler.ERROR_NOT_CONFIGURED);
    }
    else
    {
      try
      {
        handleRequest(request, response, repository);
      }
      catch (ServletException | IOException ex)
      {
        exceptionHandler.handleException(request, response, ex);
      }
    }
  }

  private void handleRequest(HttpServletRequest request,
    HttpServletResponse response, Repository repository)
    throws ServletException, IOException
  {
    if (requestListenerUtil.callListeners(request, response, repository))
    {
      Stopwatch sw = Stopwatch.createStarted();
      process(request, response, repository);
      logger.debug("mercurial request finished in {}", sw.stop());
    }
    else if (logger.isDebugEnabled())
    {
      logger.debug("request aborted by repository request listener");
    }
  }

  private void process(HttpServletRequest request,
    HttpServletResponse response, Repository repository)
    throws IOException, ServletException
  {
    CGIExecutor executor = cgiExecutorFactory.createExecutor(configuration,
                             getServletContext(), request, response);

    // issue #155
    executor.setPassShellEnvironment(true);
    executor.setExceptionHandler(exceptionHandler);
    executor.setStatusCodeHandler(exceptionHandler);
    executor.setContentLengthWorkaround(true);

    EnvList env = executor.getEnvironment();
    environmentBuilder.write(repository).forEach(env::set);

    HgConfig config = configResolver.resolve(repository);
    executor.setWorkDirectory(config.getDirectory());

    executor.setArgs(createArgs(repository, config));
    executor.execute(config.getHgBinary());
  }

  @Nonnull
  private List<String> createArgs(Repository repository, HgConfig config) {
    List<String> args = new ArrayList<>();
    config(args, "extensions.cgiserve", extension.getAbsolutePath());

    String hooks = HgExtensions.HOOK.getFile().getAbsolutePath();
    config(args, "hooks.pretxnchangegroup.scm", String.format("python:%s:pre_hook", hooks));
    config(args, "hooks.changegroup.scm", String.format("python:%s:post_hook", hooks));

    if (RepositoryPermissions.push(repository).isPermitted()) {
      config(args, "web.allow_push", "*");
    } else {
      config(args, "web.deny_push", "*");
    }

    if(RepositoryPermissions.pull(repository).isPermitted()) {
      config(args, "web.allow_read", "*");
    } else {
      config(args, "web.deny_read", "*");
    }

    config(args, "web.push_ssl", "false");

    // enable experimental httppostargs protocol of mercurial
    // Issue 970: https://goo.gl/poascp
    config(args, "experimental.httppostargs", String.valueOf(config.isEnableHttpPostArgs()));

    args.add("cgiserve");
    return args;
  }

  private void config(List<String> args, String key, String value) {
    args.add("--config");
    args.add(key + "=" + value);
  }
}
