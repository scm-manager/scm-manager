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

/**
 * @since 2.19.0
 */
public final class MirrorCommandBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(MirrorCommandBuilder.class);

  private final MirrorCommand mirrorCommand;
  private final MirrorCommandRequest request = new MirrorCommandRequest();
  private final Repository targetRepository;

  MirrorCommandBuilder(MirrorCommand mirrorCommand, Repository targetRepository) {
    this.mirrorCommand = mirrorCommand;
    this.targetRepository = targetRepository;
  }

  public MirrorCommandBuilder setCredentials(Credential credential, Credential... furtherCredentials) {
    List<Credential> credentials = new ArrayList<>();
    credentials.add(credential);
    credentials.addAll(asList(furtherCredentials));
    request.setCredentials(credentials);
    return this;
  }

  public MirrorCommandBuilder setCredentials(Collection<Credential> credentials) {
    request.setCredentials(credentials);
    return this;
  }

  public MirrorCommandBuilder setPublicKeys(PublicKey... publicKeys) {
    request.setPublicKeys(Arrays.asList(publicKeys));
    return this;
  }

  public MirrorCommandBuilder setPublicKeys(Collection<PublicKey> publicKeys) {
    request.setPublicKeys(new ArrayList<>(publicKeys));
    return this;
  }

  public MirrorCommandBuilder setSourceUrl(String sourceUrl) {
    request.setSourceUrl(sourceUrl);
    return this;
  }

  public MirrorCommandBuilder setFilter(MirrorFilter filter) {
    request.setFilter(filter);
    return this;
  }

  /**
   * If set to <code>true</code>, lfs files will not be mirrored. Defaults to <code>false</code>.
   * @return This builder instance
   * @since 2.37.0
   */
  public MirrorCommandBuilder setIgnoreLfs(boolean ignoreLfs) {
    request.setIgnoreLfs(ignoreLfs);
    return this;
  }

  /**
   * The callback that is set here will be called each time the log of the current process gets
   * updated. The log is the same that will be returned by {@link MirrorCommandResult#getLog()}
   * when the process has finished.
   * @return This builder instance
   * @since 3.12.0
   */
  public MirrorCommandBuilder setProgressCallback(LogCallback progressCallback) {
    request.setProgressCallback(progressCallback);
    return this;
  }

  /**
   * If set to <code>true</code>, the update will check the complete repository for LFS files. Otherwise only
   * new objects will be checked. This overrides {@link #setIgnoreLfs(boolean)}, so if this is set to <code>true</code>,
   * LFS files will be checked nonetheless what is set for {@link #setIgnoreLfs(boolean)}.
   * Defaults to <code>false</code>.
   * @return This builder instance
   * @since 3.12.0
   */
  public MirrorCommandBuilder setReloadLfs(boolean reloadLfs) {
    this.request.setReloadLfs(reloadLfs);
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
    request.setProxyConfiguration(proxyConfiguration);
    return this;
  }

  public MirrorCommandResult initialCall() {
    LOG.info("Creating mirror for {} in repository {}", request.getSourceUrl(), targetRepository);
    return mirrorCommand.mirror(request);
  }

  public MirrorCommandResult update() {
    LOG.debug("Updating mirror for {} in repository {}", request.getSourceUrl(), targetRepository);
    return mirrorCommand.update(request);
  }

  /**
   * This interface can be used to get information about currently running mirror processes.
   */
  public interface LogCallback {
    /**
     * This will be called each time a named step is started.
     * @param step The description of the step.
     * @param totalWork The totally expected work represented by a number. If this in unknown, it will be set to 0.
     */
    void stepStarted(String step, int totalWork);

    /**
     * Called whenever there is an update of the completed work for the last step declared by
     * {@link #stepStarted(String, int)}
     * @param completedWork The currently completed work in relation to the total work announced by
     * {@link #stepStarted(String, int)}.
     */
    void currentStepProgressed(int completedWork);

    /**
     * Called when the last step declared by {@link #stepStarted(String, int)} has been completed.
     */
    void currentStepFinished();
  }
}
