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

package sonia.scm.repository;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@EqualsAndHashCode
@ToString
@Getter
@Setter
public class SubRepository implements Serializable {

  private static final long serialVersionUID = 6960065820378492531L;

  private String browserUrl;
  private String repositoryUrl;
  private String revision;

  public SubRepository() {
  }

  public SubRepository(String repositoryUrl) {
    this.repositoryUrl = repositoryUrl;
  }

  public SubRepository(String repositoryUrl, String revision) {
    this.repositoryUrl = repositoryUrl;
    this.revision = revision;
  }

  public SubRepository(String repositoryUrl, String browserUrl, String revision) {
    this.repositoryUrl = repositoryUrl;
    this.browserUrl = browserUrl;
    this.revision = revision;
  }
}
