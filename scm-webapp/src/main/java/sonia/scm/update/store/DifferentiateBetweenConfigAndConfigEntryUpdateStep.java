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

package sonia.scm.update.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.migration.UpdateException;
import sonia.scm.store.CopyOnWrite;
import sonia.scm.version.Version;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.stream.Stream;

public class DifferentiateBetweenConfigAndConfigEntryUpdateStep {

  private static final Logger LOG = LoggerFactory.getLogger(DifferentiateBetweenConfigAndConfigEntryUpdateStep.class);

  private static final String CONFIG_HEADER = "<?xml version=\"1.0\" ?>";

  public Version getTargetVersion() {
    return Version.parse("2.0.0");
  }

  public String getAffectedDataType() {
    return "sonia.scm.dao.xml";
  }

  void updateAllInDirectory(Path configDirectory) throws IOException {
    try (Stream<Path> list = Files.list(configDirectory)) {
      list
        .filter(Files::isRegularFile)
        .filter(file -> file.toString().endsWith(".xml"))
        .filter(this::isConfigEntryFile)
        .forEach(this::updateSingleFile);
    }
  }

  private boolean isConfigEntryFile(Path potentialFile) {
    LOG.trace("Testing whether file is config entry file without mark: {}", potentialFile);
    try (InputStream inputStream = openFile(potentialFile);
      Scanner s = new Scanner(inputStream)) {
      return CONFIG_HEADER.equals(s.nextLine()) && "<configuration>".equals(s.nextLine());
    } catch (IOException e) {
      throw new UpdateException("Error reading file " + potentialFile, e);
    }
  }

  private void updateSingleFile(Path configFile) {
    LOG.info("Updating config entry file: {}", configFile);
      CopyOnWrite.withTemporaryFile(
        temporaryFile -> {
    try (OutputStream tempStream = Files.newOutputStream(temporaryFile);
         PrintStream tempPrintStream = new PrintStream(tempStream);
         InputStream inputStream = openFile(configFile);
         Scanner s = new Scanner(inputStream)) {
      tempPrintStream.println(s.nextLine()); // print xml header
      s.nextLine(); // drop line with old <configuration> tag
      tempPrintStream.println("<configuration type=\"config-entry\">"); // print correct configuration tag
      while (s.hasNextLine()) { // copy remaining file
        tempPrintStream.println(s.nextLine());
      }
    } catch (IOException e) {
      throw new UpdateException("Could not write modified config entry file", e);
    }
        }, configFile
      );
  }

  private InputStream openFile(Path potentialFile) {
    try {
      return Files.newInputStream(potentialFile);
    } catch (IOException e) {
      throw new UpdateException("Could not open file " + potentialFile, e);
    }
  }
}
