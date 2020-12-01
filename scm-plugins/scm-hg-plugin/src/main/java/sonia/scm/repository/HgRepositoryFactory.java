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

import com.aragost.javahg.RepositoryConfiguration;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.hooks.HookEnvironment;
import sonia.scm.repository.spi.javahg.HgFileviewExtension;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Map;
import java.util.function.Function;

@Singleton
public class HgRepositoryFactory {

  private static final Logger LOG = LoggerFactory.getLogger(HgRepositoryFactory.class);

  private final HgRepositoryHandler handler;
  private final HookEnvironment hookEnvironment;
  private final HgEnvironmentBuilder environmentBuilder;
  private final Function<Repository, File> directoryResolver;

  @Inject
  public HgRepositoryFactory(HgRepositoryHandler handler, HookEnvironment hookEnvironment, HgEnvironmentBuilder environmentBuilder) {
    this(
      handler, hookEnvironment, environmentBuilder,
      repository -> handler.getDirectory(repository.getId())
    );
  }

  @VisibleForTesting
  public HgRepositoryFactory(HgRepositoryHandler handler, HookEnvironment hookEnvironment, HgEnvironmentBuilder environmentBuilder, Function<Repository, File> directoryResolver) {
    this.handler = handler;
    this.hookEnvironment = hookEnvironment;
    this.environmentBuilder = environmentBuilder;
    this.directoryResolver = directoryResolver;
  }

  public com.aragost.javahg.Repository openForRead(Repository repository) {
    return open(repository, environmentBuilder.read(repository));
  }

  public com.aragost.javahg.Repository openForWrite(Repository repository) {
    return open(repository, environmentBuilder.write(repository));
  }

  private com.aragost.javahg.Repository open(Repository repository, Map<String, String> environment) {
    File directory = directoryResolver.apply(repository);

    RepositoryConfiguration repoConfiguration = RepositoryConfiguration.DEFAULT;
    repoConfiguration.getEnvironment().putAll(environment);
    repoConfiguration.addExtension(HgFileviewExtension.class);

    boolean pending = hookEnvironment.isPending();
    repoConfiguration.setEnablePendingChangesets(pending);

    Charset encoding = encoding();
    repoConfiguration.setEncoding(encoding);

    repoConfiguration.setHgBin(handler.getConfig().getHgBinary());

    LOG.trace("open hg repository {}: encoding: {}, pending: {}", directory, encoding, pending);

    return com.aragost.javahg.Repository.open(repoConfiguration, directory);
  }

  private Charset encoding() {
    String charset = handler.getConfig().getEncoding();
    try {
      return Charset.forName(charset);
    } catch (UnsupportedCharsetException ex) {
      LOG.warn("unknown charset {} in hg config, fallback to utf-8", charset);
      return StandardCharsets.UTF_8;
    }
  }
}
