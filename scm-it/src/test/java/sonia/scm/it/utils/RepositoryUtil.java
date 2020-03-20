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
    
package sonia.scm.it.utils;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Person;
import sonia.scm.repository.Tag;
import sonia.scm.repository.client.api.ClientCommand;
import sonia.scm.repository.client.api.RepositoryClient;
import sonia.scm.repository.client.api.RepositoryClientFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RepositoryUtil {

  private static final Logger LOG = LoggerFactory.getLogger(RepositoryUtil.class);

  private static final RepositoryClientFactory REPOSITORY_CLIENT_FACTORY = new RepositoryClientFactory();

  public static RepositoryClient createRepositoryClient(String repositoryType, File folder) throws IOException {
    return createRepositoryClient(repositoryType, folder, "scmadmin", "scmadmin");
  }

  public static RepositoryClient createRepositoryClient(String repositoryType, File folder, String username, String password) throws IOException {
    String httpProtocolUrl = TestData.callRepository(username, password, repositoryType, HttpStatus.SC_OK)
      .extract()
      .path("_links.protocol.find{it.name=='http'}.href");

    return REPOSITORY_CLIENT_FACTORY.create(repositoryType, httpProtocolUrl, username, password, folder);
  }

  public static RepositoryClient createAnonymousRepositoryClient(String repositoryType, File folder) throws IOException {
    String httpProtocolUrl = TestData.callRepository("scmadmin", "scmadmin", repositoryType, HttpStatus.SC_OK)
      .extract()
      .path("_links.protocol.find{it.name=='http'}.href");

    return REPOSITORY_CLIENT_FACTORY.create(repositoryType, httpProtocolUrl, folder);
  }

  public static String addAndCommitRandomFile(RepositoryClient client, String username) throws IOException {
    String uuid = UUID.randomUUID().toString();
    String name = "file-" + uuid + ".uuid";
    createAndCommitFile(client, username, name, uuid);
    return name;
  }

  public static Changeset createAndCommitFile(RepositoryClient repositoryClient, String username, String fileName, String content) throws IOException {
    writeAndAddFile(repositoryClient, fileName, content);
    return commit(repositoryClient, username, "added " + fileName);
  }

  /**
   * Bundle multiple File modification in one changeset
   *
   * @param repositoryClient
   * @param username
   * @param addedFiles       map.key: path of the file, value: the file content
   * @param modifiedFiles    map.key: path of the file, value: the file content
   * @param removedFiles     list of file paths to be removed
   * @return the changeset with all modifications
   * @throws IOException
   */
  public static Changeset commitMultipleFileModifications(RepositoryClient repositoryClient, String username, Map<String, String> addedFiles, Map<String, String> modifiedFiles, List<String> removedFiles) throws IOException {
    for (String fileName : addedFiles.keySet()) {
      writeAndAddFile(repositoryClient, fileName, addedFiles.get(fileName));
    }
    for (String fileName : modifiedFiles.keySet()) {
      writeAndAddFile(repositoryClient, fileName, modifiedFiles.get(fileName));
    }
    for (String fileName : removedFiles) {
      deleteFileAndApplyRemoveCommand(repositoryClient, fileName);
    }
    return commit(repositoryClient, username, "multiple file modifications" );
  }

  private static File writeAndAddFile(RepositoryClient repositoryClient, String fileName, String content) throws IOException {
    File file = new File(repositoryClient.getWorkingCopy(), fileName);
    Files.createParentDirs(file);
    Files.write(content, file, Charsets.UTF_8);
    addWithParentDirectories(repositoryClient, file);
    return file;
  }

  public static Changeset updateAndCommitFile(RepositoryClient repositoryClient, String username, String fileName, String content) throws IOException {
     writeAndAddFile(repositoryClient, fileName, content);
    return commit(repositoryClient, username, "updated " + fileName);
  }

  public static Changeset removeAndCommitFile(RepositoryClient repositoryClient, String username, String fileName) throws IOException {
    deleteFileAndApplyRemoveCommand(repositoryClient, fileName);
    return commit(repositoryClient, username, "removed " + fileName);
  }

  private static void deleteFileAndApplyRemoveCommand(RepositoryClient repositoryClient, String fileName) throws IOException {
    File file = new File(repositoryClient.getWorkingCopy(), fileName);
    if (repositoryClient.isCommandSupported(ClientCommand.REMOVE)) {
      repositoryClient.getRemoveCommand().remove(fileName);
    }
    file.delete();
  }

  private static String addWithParentDirectories(RepositoryClient repositoryClient, File file) throws IOException {
    File parent = file.getParentFile();
    String thisName = file.getName();
    String path;
    if (!repositoryClient.getWorkingCopy().equals(parent)) {
      path = addWithParentDirectories(repositoryClient, parent) + File.separator + thisName;
    } else {
      path = thisName;
    }
    addFile(repositoryClient, path);
    return path;
  }

  public static Changeset addFileAndCommit(RepositoryClient repositoryClient, String path, String username, String message) throws IOException {
    repositoryClient.getAddCommand().add(path);
    return commit(repositoryClient, username, message);
  }


  public static void addFile(RepositoryClient repositoryClient, String path) throws IOException {
    repositoryClient.getAddCommand().add(path);
  }

  public static Changeset commit(RepositoryClient repositoryClient, String username, String message) throws IOException {
    LOG.info("user: {} try to commit with message:  {}", username, message);
    Changeset changeset = repositoryClient.getCommitCommand().commit(new Person(username, username + "@scm-manager.org"), message);
    if (repositoryClient.isCommandSupported(ClientCommand.PUSH)) {
      repositoryClient.getPushCommand().push();
    }
    return changeset;
  }

  public static Tag addTag(RepositoryClient repositoryClient, String revision, String tagName) throws IOException {
    if (repositoryClient.isCommandSupported(ClientCommand.TAG)) {
      Tag tag = repositoryClient.getTagCommand().setRevision(revision).tag(tagName, TestData.USER_SCM_ADMIN);
      if (repositoryClient.isCommandSupported(ClientCommand.PUSH)) {
        repositoryClient.getPushCommand().pushTags();
      }
      return tag;
    }

    return null;
  }
}
