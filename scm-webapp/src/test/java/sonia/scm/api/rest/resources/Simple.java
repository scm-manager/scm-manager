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

package sonia.scm.api.rest.resources;

import sonia.scm.ModelObject;

public class Simple implements ModelObject {

  private String id;
  private String data;

  public Simple(String id, String data) {
    this.id = id;
    this.data = data;
  }

  public String getData() {
    return data;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setLastModified(Long timestamp) {

  }

  @Override
  public Long getCreationDate() {
    return null;
  }

  @Override
  public void setCreationDate(Long timestamp) {

  }

  @Override
  public Long getLastModified() {
    return null;
  }

  @Override
  public String getType() {
    return null;
  }
  @Override
  public boolean isValid() {
    return false;
  }
}
