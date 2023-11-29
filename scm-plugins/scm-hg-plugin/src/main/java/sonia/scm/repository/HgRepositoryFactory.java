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

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.javahg.RepositoryConfiguration;
import org.javahg.ext.purge.PurgeExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.hooks.HookEnvironment;
import sonia.scm.repository.spi.javahg.HgFileviewExtension;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Map;

@Singleton
public class HgRepositoryFactory {

  private static final Logger LOG = LoggerFactory.getLogger(HgRepositoryFactory.class);

  private final HgConfigResolver configResolver;
  private final HookEnvironment hookEnvironment;
  private final HgEnvironmentBuilder environmentBuilder;

  @Inject
  public HgRepositoryFactory(HgConfigResolver configResolver, HookEnvironment hookEnvironment, HgEnvironmentBuilder environmentBuilder) {
    this.configResolver = configResolver;
    this.hookEnvironment = hookEnvironment;
    this.environmentBuilder = environmentBuilder;
  }

  public org.javahg.Repository openForRead(Repository repository) {
    return open(repository, environmentBuilder.read(repository));
  }

  public org.javahg.Repository openForWrite(Repository repository) {
    return open(repository, environmentBuilder.write(repository));
  }

  private org.javahg.Repository open(Repository repository, Map<String, String> environment) {
    HgConfig config = configResolver.resolve(repository);
    File directory = config.getDirectory();

    RepositoryConfiguration repoConfiguration = RepositoryConfiguration.DEFAULT;
    repoConfiguration.getEnvironment().putAll(environment);
    repoConfiguration.addExtension(HgFileviewExtension.class);
    repoConfiguration.addExtension(PurgeExtension.class);

    boolean pending = hookEnvironment.isPending();
    repoConfiguration.setEnablePendingChangesets(pending);

    Charset encoding = encoding(config);
    repoConfiguration.setEncoding(encoding);

    repoConfiguration.setHgBin(config.getHgBinary());

    LOG.trace("open hg repository {}: encoding: {}, pending: {}", directory, encoding, pending);

    return org.javahg.Repository.open(repoConfiguration, directory);
  }

  private Charset encoding(HgConfig config) {
    String charset = config.getEncoding();
    try {
      return Charset.forName(charset);
    } catch (UnsupportedCharsetException ex) {
      LOG.warn("unknown charset {} in hg config, fallback to utf-8", charset);
      return StandardCharsets.UTF_8;
    }
  }
}
