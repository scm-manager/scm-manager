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

package sonia.scm.web;


public class HgVndMediaType {
  private static final String PREFIX = VndMediaType.PREFIX + "hgConfig";

  public static final String REPO_CONFIG = PREFIX + "-repo" + VndMediaType.SUFFIX;
  public static final String CONFIG = PREFIX + VndMediaType.SUFFIX;
  public static final String PACKAGES = PREFIX + "-packages" + VndMediaType.SUFFIX;
  public static final String INSTALLATIONS = PREFIX + "-installation" + VndMediaType.SUFFIX;

  private HgVndMediaType() {
  }
}
