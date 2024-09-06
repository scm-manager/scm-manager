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
import sonia.scm.FeatureNotSupportedException;
import sonia.scm.repository.Feature;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.spi.PushCommand;
import sonia.scm.repository.spi.PushCommandRequest;

import java.io.IOException;
import java.net.URL;
import java.util.Set;

/**
 * The push command push changes to another repository.
 *
 * @since 1.31
 */
public final class PushCommandBuilder {

  private static final Logger logger = LoggerFactory.getLogger(PushCommandBuilder.class);

  private final PushCommand command;
  private final PushCommandRequest request = new PushCommandRequest();
  private final Set<Feature> supportedFeatures;

  PushCommandBuilder(PushCommand command, Set<Feature> supportedFeatures) {
    this.command = command;
    this.supportedFeatures = supportedFeatures;
  }

  /**
   * Set username and password for request
   *
   * @param username username
   * @param password password
   * @return this builder instance.
   * @since 2.22.0
   */
  public PushCommandBuilder withUsernamePassword(String username, String password) {
    request.setUsername(username);
    request.setPassword(password);
    return this;
  }

  public PushCommandBuilder withForce(boolean force) {
    if (!supportedFeatures.contains(Feature.FORCE_PUSH)) {
      throw new FeatureNotSupportedException(Feature.FORCE_PUSH.name());
    }

    request.setForce(force);
    return this;
  }

  /**
   * Push all changes to the given remote repository.
   *
   * @param remoteRepository remote repository
   * @return information of the executed push command
   * @throws IOException
   * @throws PushFailedException when the push (maybe just partially) failed (since 2.47.0)
   */
  public PushResponse push(Repository remoteRepository) throws IOException {
    Subject subject = SecurityUtils.getSubject();

    //J-
    subject.isPermitted(RepositoryPermissions.push(remoteRepository).asShiroString());
    //J+

    logger.info("push changes to repository {}", remoteRepository);

    request.reset();
    request.setRemoteRepository(remoteRepository);

    return command.push(request);
  }

  /**
   * Push all changes to the given remote url.
   *
   * @param url url of a remote repository
   * @return information of the executed push command
   * @throws IOException
   * @throws PushFailedException when the push (maybe just partially) failed (since 2.47.0)
   * @since 1.43
   */
  public PushResponse push(String url) throws IOException {

    URL remoteUrl = new URL(url);

    logger.info("push changes to url {}", url);

    request.setRemoteUrl(remoteUrl);

    return command.push(request);
  }

}
