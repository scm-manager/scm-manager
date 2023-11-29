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

package sonia.scm.repository.spi;

import com.google.common.collect.ImmutableMap;
import jakarta.annotation.Nonnull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNPropertyValue;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.DiffFormat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SvnDiffCommandTest {

  // the smallest gif of the world
  private static final String GIF = "R0lGODlhAQABAIABAP///wAAACH5BAEKAAEALAAAAAABAAEAAAICTAEAOw==";

  private final SVNClientManager client = SVNClientManager.newInstance();

  private File repository;
  private File workingCopy;

  @BeforeEach
  void setUpDirectories(@TempDir Path directory) {
    repository = directory.resolve("repository").toFile();
    workingCopy = directory.resolve("working-copy").toFile();
  }

  @Test
  void shouldCreateGitCompatibleDiffForSinglePropChanges() throws SVNException, IOException {
    createRepository();
    commitProperty("scm:awesome", "shit");

    String diff = gitDiff("1");

    assertThat(diff).isEqualToIgnoringNewLines(String.join("\n",
        "diff --git a/ b/",
        "--- a/",
        "+++ b/",
        "@@ -0,0 +1 @@",
        " # property scm:awesome has changed",
        "+shit",
        "\\ No newline at end of property"
      ));
  }

  @Test
  void shouldCreateGitCompatibleDiffForPropChanges() throws SVNException, IOException {
    createRepository();
    commitProperties(ImmutableMap.of("one", "eins", "two", "zwei", "three", "drei"));

    String diff = gitDiff("1");

    assertThat(diff).isEqualToIgnoringNewLines(String.join("\n",
      "diff --git a/ b/",
      "--- a/",
      "+++ b/",
      "@@ -0,0 +1 @@",
      " # property one has changed",
      "+eins",
      "\\ No newline at end of property",
      "@@ -0,0 +1 @@",
      " # property two has changed",
      "+zwei",
      "\\ No newline at end of property",
      "@@ -0,0 +1 @@",
      " # property three has changed",
      "+drei",
      "\\ No newline at end of property"
    ));
  }

  @Test
  void shouldCreateGitCompatibleDiffForModifiedProp() throws SVNException, IOException {
    createRepository();
    commitProperty("scm:spaceship", "Razor Crest");
    commitProperty("scm:spaceship", "Heart Of Gold");

    String diff = gitDiff("2");

    assertThat(diff).isEqualToIgnoringNewLines(String.join("\n",
      "diff --git a/ b/",
      "--- a/",
      "+++ b/",
      "@@ -1 +1 @@",
      " # property scm:spaceship has changed",
      "-Razor Crest",
      "+Heart Of Gold",
      "\\ No newline at end of property"
    ));
  }

  @Test
  void shouldCreateGitCompatibleDiffForBinaryProps() throws SVNException, IOException {
    createRepository();

    byte[] gif = Base64.getDecoder().decode(GIF);
    commitProperty("scm:gif", gif);

    String diff = gitDiff("1");

    assertThat(diff).isEqualToIgnoringNewLines(String.join("\n",
      "diff --git a/ b/",
      "--- a/",
      "+++ b/",
      "@@ -0,0 +1 @@",
      " # property scm:gif has changed",
      "+Binary value (43 bytes)",
      "\\ No newline at end of property"
    ));
  }

  @Nonnull
  private String gitDiff(String revision) throws IOException {
    SvnDiffCommand command = createCommand();
    DiffCommandRequest request = new DiffCommandRequest();
    request.setFormat(DiffFormat.GIT);
    request.setRevision(revision);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    command.getDiffResult(request).accept(baos);
    return baos.toString();
  }

  private SvnDiffCommand createCommand() {
    return new SvnDiffCommand(new SvnContext(RepositoryTestData.createHeartOfGold(), repository));
  }

  private void commitProperty(String name, String value) throws SVNException {
    setProperty(name, SVNPropertyValue.create(value));
    commit("set property " + name + " = " + value);
  }

  private void commitProperty(String name, byte[] value) throws SVNException {
    commitProperty(name, SVNPropertyValue.create(name, value));
  }

  private void commitProperty(String name, SVNPropertyValue value) throws SVNException {
    setProperty(name, value);
    commit("set property " + name + " = " + value);
  }

  private void commit(String message) throws SVNException {
    client.getCommitClient().doCommit(
      new File[]{workingCopy},
      false,
      message,
      null,
      null,
      false,
      false,
      SVNDepth.UNKNOWN
    );
  }

  private void setProperty(String name, SVNPropertyValue value) throws SVNException {
    client.getWCClient().doSetProperty(
      workingCopy,
      name,
      value,
      true,
      SVNDepth.UNKNOWN,
      null,
      null
    );
  }

  private void commitProperties(Map<String, String> properties) throws SVNException {
    for (Map.Entry<String, String> e : properties.entrySet()) {
      setProperty(e.getKey(), SVNPropertyValue.create(e.getValue()));
    }
    commit("set " + properties.size() + " properties");
  }

  private void createRepository() throws SVNException {
    SVNURL url = SVNRepositoryFactory.createLocalRepository(repository, true, false);
    client.getUpdateClient().doCheckout(
      url,
      workingCopy,
      SVNRevision.HEAD,
      SVNRevision.HEAD,
      SVNDepth.INFINITY,
      true
    );
  }

}
