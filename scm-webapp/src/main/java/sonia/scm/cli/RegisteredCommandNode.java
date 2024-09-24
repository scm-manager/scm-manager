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

package sonia.scm.cli;

import java.util.ArrayList;
import java.util.List;

class RegisteredCommandNode {
  private final String name;
  private final Class<?> command;
  private final List<RegisteredCommandNode> children = new ArrayList<>();

  public RegisteredCommandNode(String name, Class<?> command) {
    this.name = name;
    this.command = command;
  }

  public String getName() {
    return name;
  }

  public Class<?> getCommand() {
    return command;
  }

  public List<RegisteredCommandNode> getChildren() {
    return children;
  }
}
