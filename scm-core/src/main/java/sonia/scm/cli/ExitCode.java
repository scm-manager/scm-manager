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

/**
 * @see picocli.CommandLine.ExitCode
 * @since 2.33.0
 */
public final class ExitCode {

  public static final int OK = 0;
  public static final int SERVER_ERROR = 1;
  public static final int USAGE = 2;
  public static final int NOT_FOUND = 3;

  private ExitCode() {}
}
