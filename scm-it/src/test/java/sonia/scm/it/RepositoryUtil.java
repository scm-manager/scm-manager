package sonia.scm.it;

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
import java.util.UUID;

public class RepositoryUtil {

  private static final Logger LOG = LoggerFactory.getLogger(RepositoryUtil.class);

  private static final RepositoryClientFactory REPOSITORY_CLIENT_FACTORY = new RepositoryClientFactory();

  static RepositoryClient createRepositoryClient(String repositoryType, File folder) throws IOException {
    return createRepositoryClient(repositoryType, folder, "scmadmin", "scmadmin");
  }

  static RepositoryClient createRepositoryClient(String repositoryType, File folder, String username, String password) throws IOException {
    String httpProtocolUrl = TestData.callRepository(username, password, repositoryType, HttpStatus.SC_OK)
      .extract()
      .path("_links.protocol.find{it.name=='http'}.href");

    return REPOSITORY_CLIENT_FACTORY.create(repositoryType, httpProtocolUrl, username, password, folder);
  }

  static String addAndCommitRandomFile(RepositoryClient client, String username) throws IOException {
    String uuid = UUID.randomUUID().toString();
    String name = "file-" + uuid + ".uuid";
    createAndCommitFile(client, username, name, uuid);
    return name;
  }

  static Changeset createAndCommitFile(RepositoryClient repositoryClient, String username, String fileName, String content) throws IOException {
    File file = new File(repositoryClient.getWorkingCopy(), fileName);
    Files.write(content, file, Charsets.UTF_8);
    addWithParentDirectories(repositoryClient, file);
    return commit(repositoryClient, username, "added " + fileName);
  }

  private static String addWithParentDirectories(RepositoryClient repositoryClient, File file) throws IOException {
    File parent = file.getParentFile();
    String thisName = file.getName();
    String path;
    if (!repositoryClient.getWorkingCopy().equals(parent)) {
      addWithParentDirectories(repositoryClient, parent);
      path = addWithParentDirectories(repositoryClient, parent) + File.separator + thisName;
    } else {
      path = thisName;
    }
    repositoryClient.getAddCommand().add(path);
    return path;
  }

  static Changeset commit(RepositoryClient repositoryClient, String username, String message) throws IOException {
    LOG.info("user: {} try to commit with message:  {}", username, message);
    Changeset changeset = repositoryClient.getCommitCommand().commit(new Person(username, username + "@scm-manager.org"), message);
    if (repositoryClient.isCommandSupported(ClientCommand.PUSH)) {
      repositoryClient.getPushCommand().push();
    }
    return changeset;
  }

  static Tag addTag(RepositoryClient repositoryClient, String revision, String tagName) throws IOException {
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
