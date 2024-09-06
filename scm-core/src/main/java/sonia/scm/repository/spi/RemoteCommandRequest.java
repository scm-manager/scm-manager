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

package sonia.scm.repository.spi;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import sonia.scm.repository.Repository;

import java.net.URL;

/**
 * @since 1.31
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
public abstract class RemoteCommandRequest implements Resetable {

  protected Repository remoteRepository;
  protected URL remoteUrl;
  protected String username;
  protected String password;

  /**
   * Resets the request object.
   *
   * @since 1.43
   */
  @Override
  public void reset() {
    remoteRepository = null;
    remoteUrl = null;
    username = null;
    password = null;
  }
}
