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

package sonia.scm.it;

import org.apache.http.HttpStatus;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junitpioneer.jupiter.RetryingTest;
import sonia.scm.it.utils.RepositoryUtil;
import sonia.scm.it.utils.ScmRequests;
import sonia.scm.it.utils.TestData;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.client.api.RepositoryClient;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.Files.createDirectories;
import static org.assertj.core.api.Assertions.assertThat;
import static sonia.scm.it.utils.RestUtil.ADMIN_PASSWORD;
import static sonia.scm.it.utils.RestUtil.ADMIN_USERNAME;

class DiffITCase {

  @TempDir public Path tempFolder;
  private RepositoryClient svnRepositoryClient;
  private RepositoryClient gitRepositoryClient;
  private RepositoryClient hgRepositoryClient;
  private ScmRequests.RepositoryResponse<ScmRequests.IndexResponse> svnRepositoryResponse;
  private ScmRequests.RepositoryResponse<ScmRequests.IndexResponse> hgRepositoryResponse;
  private ScmRequests.RepositoryResponse<ScmRequests.IndexResponse> gitRepositoryResponse;

  @BeforeEach
  void init() throws IOException {
    TestData.createDefault();
    String namespace = ADMIN_USERNAME;
    String repo = TestData.getDefaultRepoName("svn");
    svnRepositoryResponse =
      ScmRequests.start()
        .requestIndexResource(ADMIN_USERNAME, ADMIN_PASSWORD)
        .requestRepository(namespace, repo)
        .assertStatusCode(HttpStatus.SC_OK);
    File svnFolder = createDirectories(tempFolder.resolve("svn")).toFile();
    svnRepositoryClient = RepositoryUtil.createRepositoryClient("svn", svnFolder);

    repo = TestData.getDefaultRepoName("git");
    gitRepositoryResponse =
      ScmRequests.start()
        .requestIndexResource(ADMIN_USERNAME, ADMIN_PASSWORD)
        .requestRepository(namespace, repo)
        .assertStatusCode(HttpStatus.SC_OK);
    File gitFolder = createDirectories(tempFolder.resolve("git")).toFile();
    gitRepositoryClient = RepositoryUtil.createRepositoryClient("git", gitFolder);

    repo = TestData.getDefaultRepoName("hg");
    hgRepositoryResponse =
      ScmRequests.start()
        .requestIndexResource(ADMIN_USERNAME, ADMIN_PASSWORD)
        .requestRepository(namespace, repo)
        .assertStatusCode(HttpStatus.SC_OK);
    File hgFolder = createDirectories(tempFolder.resolve("hg")).toFile();
    hgRepositoryClient = RepositoryUtil.createRepositoryClient("hg", hgFolder);
  }

  @Test
  void shouldFindDiffsInGitFormat() throws IOException {
    String svnDiff = getDiff(RepositoryUtil.createAndCommitFile(svnRepositoryClient, ADMIN_USERNAME, "a.txt", "content of a"), svnRepositoryResponse);
    String gitDiff = getDiff(RepositoryUtil.createAndCommitFile(gitRepositoryClient, ADMIN_USERNAME, "a.txt", "content of a"), gitRepositoryResponse);
    String hgDiff = getDiff(RepositoryUtil.createAndCommitFile(hgRepositoryClient, ADMIN_USERNAME, "a.txt", "content of a"), hgRepositoryResponse);

    assertThat(Lists.newArrayList(svnDiff, gitDiff, hgDiff))
      .allSatisfy(diff -> assertThat(diff)
        .contains("diff --git "));
  }

  @Test
  void svnAddFileDiffShouldBeConvertedToGitDiff() throws IOException {
    String svnDiff = getDiff(RepositoryUtil.createAndCommitFile(svnRepositoryClient, ADMIN_USERNAME, "a.txt", "content of a"), svnRepositoryResponse);
    String gitDiff = getDiff(RepositoryUtil.createAndCommitFile(gitRepositoryClient, ADMIN_USERNAME, "a.txt", "content of a"), gitRepositoryResponse);

    String expected = getGitDiffWithoutIndexLine(gitDiff);
    assertDiffsAreEqual(svnDiff, expected);
  }

  @RetryingTest(3)
  void svnDeleteFileDiffShouldBeConvertedToGitDiff() throws IOException {
    RepositoryUtil.createAndCommitFile(svnRepositoryClient, ADMIN_USERNAME, "a.txt", "content of a");
    RepositoryUtil.createAndCommitFile(gitRepositoryClient, ADMIN_USERNAME, "a.txt", "content of a");

    String svnDiff = getDiff(RepositoryUtil.removeAndCommitFile(svnRepositoryClient, ADMIN_USERNAME, "a.txt"), svnRepositoryResponse);
    String gitDiff = getDiff(RepositoryUtil.removeAndCommitFile(gitRepositoryClient, ADMIN_USERNAME, "a.txt"), gitRepositoryResponse);

    String expected = getGitDiffWithoutIndexLine(gitDiff);
    assertDiffsAreEqual(svnDiff, expected);
  }

  @RetryingTest(3)
  void svnUpdateFileDiffShouldBeConvertedToGitDiff() throws IOException {
    RepositoryUtil.createAndCommitFile(svnRepositoryClient, ADMIN_USERNAME, "a.txt", "content of a");
    RepositoryUtil.createAndCommitFile(gitRepositoryClient, ADMIN_USERNAME, "a.txt", "content of a");

    String svnDiff = getDiff(RepositoryUtil.updateAndCommitFile(svnRepositoryClient, ADMIN_USERNAME, "a.txt", "the updated content of a"), svnRepositoryResponse);
    String gitDiff = getDiff(RepositoryUtil.updateAndCommitFile(gitRepositoryClient, ADMIN_USERNAME, "a.txt", "the updated content of a"), gitRepositoryResponse);

    String expected = getGitDiffWithoutIndexLine(gitDiff);
    assertDiffsAreEqual(svnDiff, expected);
  }

  @RetryingTest(3)
  void svnMultipleChangesDiffShouldBeConvertedToGitDiff() throws IOException {
    String svnDiff = getDiff(applyMultipleChanges(svnRepositoryClient, "fileToBeDeleted.txt", "fileToBeUpdated.txt", "addedFile.txt"), svnRepositoryResponse);
    String gitDiff = getDiff(applyMultipleChanges(gitRepositoryClient, "fileToBeDeleted.txt", "fileToBeUpdated.txt", "addedFile.txt"), gitRepositoryResponse);

    String endOfDiffPart = "\\ No newline at end of file\n";
    String[] gitDiffs = gitDiff.split(endOfDiffPart);
    List<String> expected = Arrays.stream(gitDiffs)
      .map(this::getGitDiffWithoutIndexLine)
      .collect(Collectors.toList());
    assertThat(svnDiff.split(endOfDiffPart))
      .containsExactlyInAnyOrderElementsOf(expected);
  }

  @Test
  void svnMultipleSubFolderChangesDiffShouldBeConvertedToGitDiff() throws IOException {
    String svnDiff = getDiff(applyMultipleChanges(svnRepositoryClient, "a/b/fileToBeDeleted.txt", "a/c/fileToBeUpdated.txt", "a/d/addedFile.txt"), svnRepositoryResponse);
    String gitDiff = getDiff(applyMultipleChanges(gitRepositoryClient, "a/b/fileToBeDeleted.txt", "a/c/fileToBeUpdated.txt", "a/d/addedFile.txt"), gitRepositoryResponse);

    String endOfDiffPart = "\\ No newline at end of file\n";
    String[] gitDiffs = gitDiff.split(endOfDiffPart);
    List<String> expected = Arrays.stream(gitDiffs)
      .map(this::getGitDiffWithoutIndexLine)
      .collect(Collectors.toList());
    assertThat(svnDiff.split(endOfDiffPart))
      .containsExactlyInAnyOrderElementsOf(expected);
  }

  @RetryingTest(3)
  void svnLargeChangesDiffShouldBeConvertedToGitDiff() throws IOException, URISyntaxException {
    String fileName = "SvnDiffGenerator_forTest";
    RepositoryUtil.createAndCommitFile(svnRepositoryClient, ADMIN_USERNAME, fileName, "");
    RepositoryUtil.createAndCommitFile(gitRepositoryClient, ADMIN_USERNAME, fileName, "");

    String fileContent = getFileContent("/diff/largefile/original/SvnDiffGenerator_forTest");
    String svnDiff = getDiff(RepositoryUtil.updateAndCommitFile(svnRepositoryClient, ADMIN_USERNAME, fileName, fileContent), svnRepositoryResponse);
    String gitDiff = getDiff(RepositoryUtil.updateAndCommitFile(gitRepositoryClient, ADMIN_USERNAME, fileName, fileContent), gitRepositoryResponse);
    assertDiffsAreEqual(svnDiff, getGitDiffWithoutIndexLine(gitDiff));

    fileContent = getFileContent("/diff/largefile/modified/v1/SvnDiffGenerator_forTest");
    svnDiff = getDiff(RepositoryUtil.updateAndCommitFile(svnRepositoryClient, ADMIN_USERNAME, fileName, fileContent), svnRepositoryResponse);
    gitDiff = getDiff(RepositoryUtil.updateAndCommitFile(gitRepositoryClient, ADMIN_USERNAME, fileName, fileContent), gitRepositoryResponse);
    assertDiffsAreEqual(svnDiff, getGitDiffWithoutIndexLine(gitDiff));

    fileContent = getFileContent("/diff/largefile/modified/v2/SvnDiffGenerator_forTest");
    svnDiff = getDiff(RepositoryUtil.updateAndCommitFile(svnRepositoryClient, ADMIN_USERNAME, fileName, fileContent), svnRepositoryResponse);
    gitDiff = getDiff(RepositoryUtil.updateAndCommitFile(gitRepositoryClient, ADMIN_USERNAME, fileName, fileContent), gitRepositoryResponse);
    assertDiffsAreEqual(svnDiff, getGitDiffWithoutIndexLine(gitDiff));
  }

  /**
   * FIXME: the binary Git Diff output is not GIT conform
   */
  @Test
  @Disabled
  @SuppressWarnings("squid:S1607")
  void svnBinaryChangesDiffShouldBeConvertedToGitDiff() throws IOException, URISyntaxException {
    String fileName = "binary";
    File file = new File(svnRepositoryClient.getWorkingCopy(), fileName);
    Files.copy(Paths.get(getClass().getResource("/diff/binaryfile/echo").toURI()), Paths.get(file.toURI()));
    Changeset commit = RepositoryUtil.addFileAndCommit(svnRepositoryClient, fileName, ADMIN_USERNAME, "");

    file = new File(gitRepositoryClient.getWorkingCopy(), fileName);
    Files.copy(Paths.get(getClass().getResource("/diff/binaryfile/echo").toURI()), Paths.get(file.toURI()));

    Changeset commit1 = RepositoryUtil.addFileAndCommit(gitRepositoryClient, fileName, ADMIN_USERNAME, "");
    String svnDiff = getDiff(commit, svnRepositoryResponse);
    String gitDiff = getDiff(commit1, gitRepositoryResponse);
    assertDiffsAreEqual(svnDiff, getGitDiffWithoutIndexLine(gitDiff));

  }

  @RetryingTest(3)
  void svnRenameChangesDiffShouldBeConvertedToGitDiff() throws IOException, URISyntaxException {
    String fileName = "a.txt";
    RepositoryUtil.createAndCommitFile(svnRepositoryClient, ADMIN_USERNAME, fileName, "content of a");
    RepositoryUtil.createAndCommitFile(gitRepositoryClient, ADMIN_USERNAME, fileName, "content of a");

    String newFileName = "renamed_a.txt";
    File file = new File(svnRepositoryClient.getWorkingCopy(), fileName);
    file.renameTo(new File(svnRepositoryClient.getWorkingCopy(), newFileName));

    String svnDiff = getDiff(RepositoryUtil.addFileAndCommit(svnRepositoryClient, newFileName, ADMIN_USERNAME, "renamed file"), svnRepositoryResponse);

    file = new File(gitRepositoryClient.getWorkingCopy(), fileName);
    file.renameTo(new File(gitRepositoryClient.getWorkingCopy(), newFileName));
    String gitDiff = getDiff(RepositoryUtil.addFileAndCommit(gitRepositoryClient, newFileName, ADMIN_USERNAME, "renamed file"), gitRepositoryResponse);

    String expected = getGitDiffWithoutIndexLine(gitDiff);
    assertDiffsAreEqual(svnDiff, expected);
  }

  public String getFileContent(String name) throws URISyntaxException, IOException {
    Path path;
    path = Paths.get(getClass().getResource(name).toURI());
    Stream<String> lines = Files.lines(path);
    String data = lines.collect(Collectors.joining("\n"));
    lines.close();
    return data;
  }

  /**
   * The index line is not provided from the svn git formatter and it is not needed in the ui diff view
   * for more details about the git diff format: https://git-scm.com/docs/git-diff
   *
   * @param gitDiff
   * @return diff without the index line
   */
  private String getGitDiffWithoutIndexLine(String gitDiff) {
    return gitDiff.replaceAll(".*(index.*\n)", "");
  }

  private void assertDiffsAreEqual(String svnDiff, String gitDiff) {
    assertThat(svnDiff)
      .as("diffs are different\n\nsvn:\n==================================================\n\n%s\n\ngit:\n==================================================\n\n%s)", svnDiff, gitDiff)
      .isEqualTo(gitDiff);
  }

  private String getDiff(Changeset svnChangeset, ScmRequests.RepositoryResponse<ScmRequests.IndexResponse> svnRepositoryResponse) {
    return svnRepositoryResponse.requestChangesets()
      .requestDiffInGitFormat(svnChangeset.getId())
      .getResponse()
      .body()
      .asString();
  }

  private Changeset applyMultipleChanges(RepositoryClient repositoryClient, String fileToBeDeleted, final String fileToBeUpdated, final String addedFile) throws IOException {
    RepositoryUtil.createAndCommitFile(repositoryClient, ADMIN_USERNAME, fileToBeDeleted, "file to be deleted");
    RepositoryUtil.createAndCommitFile(repositoryClient, ADMIN_USERNAME, fileToBeUpdated, "file to be updated");
    Map<String, String> addedFiles = new HashMap<String, String>() {{
      put(addedFile, "content");
    }};
    Map<String, String> modifiedFiles = new HashMap<String, String>() {{
      put(fileToBeUpdated, "the updated content");
    }};
    ArrayList<String> removedFiles = Lists.newArrayList(fileToBeDeleted);
    return RepositoryUtil.commitMultipleFileModifications(repositoryClient, ADMIN_USERNAME, addedFiles, modifiedFiles, removedFiles);
  }
}
