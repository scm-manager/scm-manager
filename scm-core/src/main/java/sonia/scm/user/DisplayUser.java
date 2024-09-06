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

package sonia.scm.user;

import sonia.scm.ReducedModelObject;

public class DisplayUser implements ReducedModelObject {

  private final String id;
  private final String displayName;
  private final String mail;

  public static DisplayUser from(User user) {
    return new DisplayUser(user.getId(), user.getDisplayName(), user.getMail());
  }

  private DisplayUser(String id, String displayName, String mail) {
    this.id = id;
    this.displayName = displayName;
    this.mail = mail;
  }

  public String getId() {
    return id;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getMail() {
    return mail;
  }
}
