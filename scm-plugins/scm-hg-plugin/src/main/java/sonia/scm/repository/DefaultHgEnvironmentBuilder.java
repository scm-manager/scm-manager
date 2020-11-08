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

package sonia.scm.repository;


import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import sonia.scm.repository.hooks.HookEnvironment;
import sonia.scm.repository.hooks.HookServer;
import sonia.scm.security.AccessToken;
import sonia.scm.security.AccessTokenBuilderFactory;
import sonia.scm.security.CipherUtil;
import sonia.scm.security.Xsrf;
import sonia.scm.web.HgUtil;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class DefaultHgEnvironmentBuilder implements HgEnvironmentBuilder {

  @VisibleForTesting
  static final String ENV_PYTHON_PATH = "PYTHONPATH";
  @VisibleForTesting
  static final String ENV_HOOK_PORT = "SCM_HOOK_PORT";
  @VisibleForTesting
  static final String ENV_CHALLENGE = "SCM_CHALLENGE";
  @VisibleForTesting
  static final String ENV_BEARER_TOKEN = "SCM_BEARER_TOKEN";
  @VisibleForTesting
  static final String ENV_REPOSITORY_NAME = "REPO_NAME";
  @VisibleForTesting
  static final String ENV_REPOSITORY_PATH = "SCM_REPOSITORY_PATH";
  @VisibleForTesting
  static final String ENV_REPOSITORY_ID = "SCM_REPOSITORY_ID";
  @VisibleForTesting
  static final String ENV_HTTP_POST_ARGS = "SCM_HTTP_POST_ARGS";

  private final AccessTokenBuilderFactory accessTokenBuilderFactory;
  private final HgRepositoryHandler repositoryHandler;
  private final HookEnvironment hookEnvironment;
  private final HookServer server;

  private int hookPort = -1;

  @Inject
  public DefaultHgEnvironmentBuilder(
    AccessTokenBuilderFactory accessTokenBuilderFactory, HgRepositoryHandler repositoryHandler,
    HookEnvironment hookEnvironment, HookServer server
  ) {
    this.accessTokenBuilderFactory = accessTokenBuilderFactory;
    this.repositoryHandler = repositoryHandler;
    this.hookEnvironment = hookEnvironment;
    this.server = server;
  }


  @Override
  public Map<String, String> read(Repository repository) {
    ImmutableMap.Builder<String, String> env = ImmutableMap.builder();
    read(env, repository);
    return env.build();
  }

  @Override
  public Map<String, String> write(Repository repository) {
    ImmutableMap.Builder<String, String> env = ImmutableMap.builder();
    read(env, repository);
    write(env);
    return env.build();
  }

  private void read(ImmutableMap.Builder<String, String> env, Repository repository) {
    HgConfig config = repositoryHandler.getConfig();
    env.put(ENV_PYTHON_PATH, HgUtil.getPythonPath(config));

    File directory = repositoryHandler.getDirectory(repository.getId());

    env.put(ENV_REPOSITORY_NAME, repository.getNamespace() + "/" + repository.getName());
    env.put(ENV_REPOSITORY_ID, repository.getId());
    env.put(ENV_REPOSITORY_PATH, directory.getAbsolutePath());

    // enable experimental httppostargs protocol of mercurial
    // Issue 970: https://goo.gl/poascp
    env.put(ENV_HTTP_POST_ARGS, String.valueOf(config.isEnableHttpPostArgs()));
  }

  private void write(ImmutableMap.Builder<String, String> env) {
    env.put(ENV_HOOK_PORT, String.valueOf(getHookPort()));
    env.put(ENV_BEARER_TOKEN, accessToken());
    env.put(ENV_CHALLENGE, hookEnvironment.getChallenge());
  }

  private String accessToken() {
    AccessToken accessToken = accessTokenBuilderFactory.create()
      // disable xsrf protection, because we can not access the http servlet request for verification
      .custom(Xsrf.TOKEN_KEY, null)
      .build();
    return CipherUtil.getInstance().encode(accessToken.compact());
  }

  private synchronized int getHookPort() {
    if (hookPort > 0) {
      return hookPort;
    }
    try {
      hookPort = server.start();
    } catch (IOException ex) {
      throw new IllegalStateException("failed to start mercurial hook server");
    }
    return hookPort;
  }

}
