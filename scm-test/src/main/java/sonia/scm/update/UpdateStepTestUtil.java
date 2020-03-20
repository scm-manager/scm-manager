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
    
package sonia.scm.update;

import com.google.common.io.Resources;
import org.mockito.Mockito;
import sonia.scm.SCMContextProvider;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

public class UpdateStepTestUtil {

  private final SCMContextProvider contextProvider;

  private final Path tempDir;

  public UpdateStepTestUtil(Path tempDir) {
    this.tempDir = tempDir;
    contextProvider = Mockito.mock(SCMContextProvider.class);
    lenient().when(contextProvider.getBaseDirectory()).thenReturn(tempDir.toFile());
    lenient().when(contextProvider.resolve(any())).thenAnswer(invocation -> tempDir.resolve(invocation.getArgument(0).toString()));
  }

  public SCMContextProvider getContextProvider() {
    return contextProvider;
  }

  public void copyConfigFile(String fileName) throws IOException {
    Path configDir = tempDir.resolve("config");
    Files.createDirectories(configDir);
    copyTestDatabaseFile(configDir, fileName);
  }

  public void copyConfigFile(String fileName, String targetFileName) throws IOException {
    Path configDir = tempDir.resolve("config");
    Files.createDirectories(configDir);
    copyTestDatabaseFile(configDir, fileName, targetFileName);
  }

  private void copyTestDatabaseFile(Path configDir, String fileName) throws IOException {
    Path targetFileName = Paths.get(fileName).getFileName();
    copyTestDatabaseFile(configDir, fileName, targetFileName.toString());
  }

  private void copyTestDatabaseFile(Path configDir, String fileName, String targetFileName) throws IOException {
    URL url = Resources.getResource(fileName);
    Files.copy(url.openStream(), configDir.resolve(targetFileName));
  }
}
