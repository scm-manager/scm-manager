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

import sonia.scm.repository.HgEnvironment;
import sonia.scm.repository.HgHookManager;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.Repository;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Map;

public class HgRepositoryEnvironmentBuilder {

  private static final String ENV_REPOSITORY_NAME = "REPO_NAME";
  private static final String ENV_REPOSITORY_PATH = "SCM_REPOSITORY_PATH";
  private static final String ENV_REPOSITORY_ID = "SCM_REPOSITORY_ID";
  private static final String ENV_PYTHON_HTTPS_VERIFY = "PYTHONHTTPSVERIFY";
  private static final String ENV_HTTP_POST_ARGS = "SCM_HTTP_POST_ARGS";

  private final HgRepositoryHandler handler;
  private final HgHookManager hookManager;

  @Inject
  public HgRepositoryEnvironmentBuilder(HgRepositoryHandler handler, HgHookManager hookManager) {
    this.handler = handler;
    this.hookManager = hookManager;
  }

  public void buildFor(Repository repository, HttpServletRequest request, Map<String, String> environment) {
    File directory = handler.getDirectory(repository.getId());

    environment.put(ENV_REPOSITORY_NAME, repository.getNamespace() + "/" + repository.getName());
    environment.put(ENV_REPOSITORY_ID, repository.getId());
    environment.put(ENV_REPOSITORY_PATH,
      directory.getAbsolutePath());

    // add hook environment
    if (handler.getConfig().isDisableHookSSLValidation()) {
      // disable ssl validation
      // Issue 959: https://goo.gl/zH5eY8
      environment.put(ENV_PYTHON_HTTPS_VERIFY, "0");
    }

    // enable experimental httppostargs protocol of mercurial
    // Issue 970: https://goo.gl/poascp
    environment.put(ENV_HTTP_POST_ARGS, String.valueOf(handler.getConfig().isEnableHttpPostArgs()));

    HgEnvironment.prepareEnvironment(
      environment,
      handler,
      hookManager,
      request
    );
  }
}
