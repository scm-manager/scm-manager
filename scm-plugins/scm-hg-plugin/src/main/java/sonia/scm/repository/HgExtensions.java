/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.repository;


import sonia.scm.SCMContext;
import sonia.scm.SCMContextProvider;

import java.io.File;


public enum HgExtensions {

  HOOK("scmhooks.py"),
  CGISERVE("cgiserve.py"),
  VERSION("scmversion.py"),
  FILEVIEW("fileview.py");

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
