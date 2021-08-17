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

package sonia.scm.repository.api;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Repository;
import sonia.scm.repository.spi.MirrorCommand;
import sonia.scm.repository.spi.MirrorCommandRequest;
import sonia.scm.security.PublicKey;
import sonia.scm.net.ProxyConfiguration;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

/**
 * @since 2.19.0
 */
@Beta
public final class MirrorCommandBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(MirrorCommandBuilder.class);

  private final MirrorCommand mirrorCommand;
  private final Repository targetRepository;

  private String sourceUrl;
  private Collection<Credential> credentials = emptyList();
  private List<PublicKey> publicKeys = emptyList();
  private MirrorFilter filter = new MirrorFilter() {};

  @Nullable
  private ProxyConfiguration proxyConfiguration;

  MirrorCommandBuilder(MirrorCommand mirrorCommand, Repository targetRepository) {
    this.mirrorCommand = mirrorCommand;
    this.targetRepository = targetRepository;
  }

  public MirrorCommandBuilder setCredentials(Credential credential, Credential... furtherCredentials) {
    this.credentials = new ArrayList<>();
    credentials.add(credential);
    credentials.addAll(asList(furtherCredentials));
    return this;
  }

  public MirrorCommandBuilder setCredentials(Collection<Credential> credentials) {
    this.credentials = credentials;
    return this;
  }

  public MirrorCommandBuilder setPublicKeys(PublicKey... publicKeys) {
    this.publicKeys = Arrays.asList(publicKeys);
    return this;
  }

  public MirrorCommandBuilder setPublicKeys(Collection<PublicKey> publicKeys) {
    this.publicKeys = new ArrayList<>(publicKeys);
    return this;
  }

  public MirrorCommandBuilder setSourceUrl(String sourceUrl) {
    this.sourceUrl = sourceUrl;
    return this;
  }

  public MirrorCommandBuilder setFilter(MirrorFilter filter) {
    this.filter = filter;
    return this;
  }

  /**
   * Set the proxy configuration which should be used to access the source repository of the mirror.
   * If not proxy configuration is set the global configuration should be used instead.
   * @param proxyConfiguration proxy configuration to access the source repository
   * @return {@code this}
   * @since 2.23.0
   */
  public MirrorCommandBuilder setProxyConfiguration(ProxyConfiguration proxyConfiguration) {
    this.proxyConfiguration = proxyConfiguration;
    return this;
  }

  public MirrorCommandResult initialCall() {
    LOG.info("Creating mirror for {} in repository {}", sourceUrl, targetRepository);
    MirrorCommandRequest mirrorCommandRequest = createRequest();
    return mirrorCommand.mirror(mirrorCommandRequest);
  }

  public MirrorCommandResult update() {
    LOG.debug("Updating mirror for {} in repository {}", sourceUrl, targetRepository);
    MirrorCommandRequest mirrorCommandRequest = createRequest();
    return mirrorCommand.update(mirrorCommandRequest);
  }

  private MirrorCommandRequest createRequest() {
    MirrorCommandRequest mirrorCommandRequest = new MirrorCommandRequest();
    mirrorCommandRequest.setSourceUrl(sourceUrl);
    mirrorCommandRequest.setCredentials(credentials);
    mirrorCommandRequest.setFilter(filter);
    mirrorCommandRequest.setPublicKeys(publicKeys);
    mirrorCommandRequest.setProxyConfiguration(proxyConfiguration);
    Preconditions.checkArgument(mirrorCommandRequest.isValid(), "source url has to be specified");
    return mirrorCommandRequest;
  }
}
