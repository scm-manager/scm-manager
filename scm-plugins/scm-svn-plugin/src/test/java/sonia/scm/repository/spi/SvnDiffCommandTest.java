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
import sonia.scm.repository.api.IgnoreWhitespaceLevel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
  void shouldCreateDiffForSimpleFile() throws SVNException, IOException {
    createRepository();
    Path newFile = workingCopy.toPath().resolve("a.txt");
    Files.write(newFile, "Some nice content\n".getBytes());
    client.getWCClient().doAdd(newFile.toFile(), false, false, false, SVNDepth.INFINITY, false, false);
    commit("add a.txt");

    Files.write(newFile, "Some more content\n".getBytes());
    commit("modify a.txt");

    String diff = gitDiff("2");

    assertThat(diff).isEqualTo("""
diff --git a/a.txt b/a.txt
--- a/a.txt
+++ b/a.txt
@@ -1 +1 @@
-Some nice content
+Some more content
""");
  }

  @Test
  void shouldIgnoreWhitespaceChanges() throws SVNException, IOException {
    createRepository();
    Path newFile = workingCopy.toPath().resolve("a.txt");
    Files.write(newFile, "Some nice content\n".getBytes());
    client.getWCClient().doAdd(newFile.toFile(), false, false, false, SVNDepth.INFINITY, false, false);
    commit("add a.txt");

    Files.write(newFile, "Some  nice  content \n".getBytes());
    commit("modify a.txt");

    DiffCommandRequest request = createSimpleDiffRequest("2");
    request.setIgnoreWhitespaceLevel(IgnoreWhitespaceLevel.ALL);
    String diff = executeDiff(request);

    assertThat(diff).isEqualTo("diff --git a/a.txt b/a.txt\n");
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
    DiffCommandRequest request = createSimpleDiffRequest(revision);
    return executeDiff(request);
  }

  private String executeDiff(DiffCommandRequest request) throws IOException {
    SvnDiffCommand command = createCommand();

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    command.getDiffResult(request).accept(baos);
    return baos.toString();
  }

  private static DiffCommandRequest createSimpleDiffRequest(String revision) {
    DiffCommandRequest request = new DiffCommandRequest();
    request.setFormat(DiffFormat.GIT);
    request.setRevision(revision);
    return request;
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
