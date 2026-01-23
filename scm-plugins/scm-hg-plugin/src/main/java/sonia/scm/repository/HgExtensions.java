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


import sonia.scm.SCMContext;
import sonia.scm.SCMContextProvider;

import java.io.File;


public enum HgExtensions {

  HOOK("scmhooks.py"),
  CGISERVE("cgiserve.py"),
  VERSION("scmversion.py"),
  FILEVIEW("fileview.py"),
  CONFIGFILE("configfile.py");

  private static final String BASE_DIRECTORY = "lib".concat(File.separator).concat("python");
  private static final String BASE_RESOURCE = "/sonia/scm/python/";

  private final String name;

  HgExtensions(String name) {
    this.name = name;
  }

  public static File getScriptDirectory(SCMContextProvider context) {
    return new File(context.getBaseDirectory(), BASE_DIRECTORY);
  }

  public File getFile() {
    return getFile(SCMContext.getContext());
  }

  public File getFile(SCMContextProvider context) {
    return new File(getScriptDirectory(context), name);
  }

  public String getName() {
    return name;
  }

  public String getResourcePath() {
    return BASE_RESOURCE.concat(name);
  }
}
