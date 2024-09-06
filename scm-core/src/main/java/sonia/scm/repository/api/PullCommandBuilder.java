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

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.spi.PullCommand;
import sonia.scm.repository.spi.PullCommandRequest;

import java.io.IOException;
import java.net.URL;

/**
 * The pull command pull changes from a other repository.
 *
 * @since 1.31
 */
public final class PullCommandBuilder {

  private static final Logger logger = LoggerFactory.getLogger(PullCommandBuilder.class);

  private final PullCommand command;
  private final Repository localRepository;
  private final PullCommandRequest request = new PullCommandRequest();

  PullCommandBuilder(PullCommand command, Repository localRepository) {
    this.command = command;
    this.localRepository = localRepository;
    request.reset();
  }

  /**
   * Set username for authentication
   *
   * @param username username
   * @return this builder instance.
   * @since 2.11.0
   */
  public PullCommandBuilder withUsername(String username) {
    request.setUsername(username);
    return this;
  }

  /**
   * Set password for authentication
   *
   * @param password password
   * @return this builder instance.
   * @since 2.11.0
   */
  public PullCommandBuilder withPassword(String password) {
    request.setPassword(password);
    return this;
  }

  /**
   * Set whether to fetch LFS files (<code>true</code>) or not (<code>false</code>).
   * This may not work for all repository types.
   *
   * @param fetchLfs Whether to fetch LFS files or not
   * @return this builder instance.
   * @since 2.40.0
   */
  public PullCommandBuilder doFetchLfs(boolean fetchLfs) {
    request.setFetchLfs(fetchLfs);
    return this;
  }

  /**
   * Pull all changes from the given remote url.
   *
   * @param url remote url
   * @return information over the executed pull command
   * @throws IOException
   * @since 1.43
   */
  public PullResponse pull(String url) throws IOException {
    Subject subject = SecurityUtils.getSubject();
    //J-
    subject.isPermitted(RepositoryPermissions.push(localRepository).asShiroString());
    //J+

    URL remoteUrl = new URL(url);
    request.setRemoteUrl(remoteUrl);

    logger.info("pull changes from url {}", url);

    return command.pull(request);
  }

  /**
   * Pull all changes from the given remote repository.
   *
   * @param remoteRepository remote repository
   * @return information over the executed pull command
   * @throws IOException
   */
  public PullResponse pull(Repository remoteRepository) throws IOException {
    Subject subject = SecurityUtils.getSubject();

    //J-
    subject.isPermitted(RepositoryPermissions.push(localRepository).asShiroString());
    subject.isPermitted(RepositoryPermissions.push(remoteRepository).asShiroString());
    //J+

    request.reset();
    request.setRemoteRepository(remoteRepository);

    logger.info("pull changes from {}", remoteRepository);

    return command.pull(request);
  }
}
