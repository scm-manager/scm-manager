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
