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

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SvnDiffCommandTest {

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

    System.out.println(diff);

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

    System.out.println(diff);

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

  private void setProperty(String name, String value) throws SVNException {
    client.getWCClient().doSetProperty(
      workingCopy,
      name,
      SVNPropertyValue.create(value),
      true,
      SVNDepth.UNKNOWN,
      null,
      null
    );
  }

  private void commitProperties(Map<String, String> properties) throws SVNException {
    for (Map.Entry<String, String> e : properties.entrySet()) {
      setProperty(e.getKey(), e.getValue());
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
