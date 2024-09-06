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

package sonia.scm.repository.api;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.net.ProxyConfiguration;
import sonia.scm.repository.Repository;
import sonia.scm.repository.spi.MirrorCommand;
import sonia.scm.repository.spi.MirrorCommandRequest;
import sonia.scm.security.PublicKey;

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
  private boolean ignoreLfs;

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
   * If set to <code>true</code>, lfs files will not be mirrored. Defaults to <code>false</code>.
   * @return This builder instance
   * @since 2.37.0
   */
  public MirrorCommandBuilder setIgnoreLfs(boolean ignoreLfs) {
    this.ignoreLfs = ignoreLfs;
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
    mirrorCommandRequest.setIgnoreLfs(ignoreLfs);
    Preconditions.checkArgument(mirrorCommandRequest.isValid(), "source url has to be specified");
    return mirrorCommandRequest;
  }
}
