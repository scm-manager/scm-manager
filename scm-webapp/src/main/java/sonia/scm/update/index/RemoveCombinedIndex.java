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

package sonia.scm.update.index;

import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import sonia.scm.SCMContextProvider;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.util.IOUtil;
import sonia.scm.version.Version;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static sonia.scm.store.StoreConstants.DATA_DIRECTORY_NAME;
import static sonia.scm.store.StoreConstants.VARIABLE_DATA_DIRECTORY_NAME;

@Extension
public class RemoveCombinedIndex implements UpdateStep {

  private final SCMContextProvider contextProvider;

  @Inject
  public RemoveCombinedIndex(SCMContextProvider contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public void doUpdate() throws IOException {
    Path index = contextProvider.resolve(Paths.get("index"));
    if (Files.exists(index)) {
      IOUtil.delete(index.toFile());
    }

    Path indexLog = contextProvider.resolve(indexLogPath());
    if (Files.exists(indexLog)) {
      IOUtil.delete(indexLog.toFile());
    }
  }

  @Nonnull
  private Path indexLogPath() {
    return Paths.get(VARIABLE_DATA_DIRECTORY_NAME).resolve(DATA_DIRECTORY_NAME).resolve("index-log");
  }

  @Override
  public Version getTargetVersion() {
    return Version.parse("2.0.0");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.index";
  }
}
