package sonia.scm.it;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.http.HttpStatus;
import org.junit.rules.TemporaryFolder;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Person;
import sonia.scm.repository.client.api.ClientCommand;
import sonia.scm.repository.client.api.RepositoryClient;
import sonia.scm.repository.client.api.RepositoryClientException;
import sonia.scm.repository.client.api.RepositoryClientFactory;
import sonia.scm.web.VndMediaType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import static sonia.scm.it.RestUtil.given;

public class RepositoryUtil {

  private static final RepositoryClientFactory REPOSITORY_CLIENT_FACTORY = new RepositoryClientFactory();

  static void addRandomFileToRepository(RepositoryClient client) throws IOException {
    String uuid = UUID.randomUUID().toString();
    String name = "file-" + uuid + ".uuid";

    File file = new File(client.getWorkingCopy(), name);
    try (FileOutputStream out = new FileOutputStream(file)) {
      out.write(uuid.getBytes());
    }
    client.getAddCommand().add(name);
  }

  static boolean canScmAdminCommit(String repositoryType, TemporaryFolder temporaryFolder) throws IOException {
    return canUserCommit("scmadmin", "scmadmin", repositoryType, temporaryFolder);
  }

  static boolean canUserCommit(String username, String password, String repositoryType, TemporaryFolder temporaryFolder) throws IOException {
    RepositoryClient client = RepositoryUtil.createRepositoryClient(repositoryType, temporaryFolder.newFolder(), username, password);
    for (int i = 0; i < 5; i++) {
      addRandomFileToRepository(client);
    }
    try{
      commit(client, username, "commit");
    }catch (RepositoryClientException e){
      return false;
    }
    RepositoryClient checkClient = RepositoryUtil.createRepositoryClient(repositoryType, temporaryFolder.newFolder(), username, password);
    return checkClient.getWorkingCopy().list().length == 6;
  }

  static RepositoryClient createRepositoryClient(String repositoryType, File folder) throws IOException {
    return createRepositoryClient(repositoryType, folder, "scmadmin", "scmadmin");
  }

  static RepositoryClient createRepositoryClient(String repositoryType, File folder, String username, String password) throws IOException {
    String httpProtocolUrl = given(VndMediaType.REPOSITORY, username, password)

      .when()
      .get(TestData.getDefaultRepositoryUrl(repositoryType))

      .then()
      .statusCode(HttpStatus.SC_OK)
      .extract()
      .path("_links.httpProtocol.href");

    return REPOSITORY_CLIENT_FACTORY.create(repositoryType, httpProtocolUrl, username, password, folder);
  }
  static void assertDeleteRepositoryOperation(String user, int deleteStatus, int getStatus, String password, String repositoryType) {
    given(VndMediaType.REPOSITORY, user, password)

      .when()
      .delete(TestData.getDefaultRepositoryUrl(repositoryType))

      .then()
      .statusCode(deleteStatus);

    given(VndMediaType.REPOSITORY, user, password)

      .when()
      .get(TestData.getDefaultRepositoryUrl(repositoryType))

      .then()
      .statusCode(getStatus);
  }
  static void createAndCommitFile(File folder, RepositoryClient repositoryClient, String username, String fileName, String content) throws IOException {
    Files.write(content, new File(folder, fileName), Charsets.UTF_8);
    repositoryClient.getAddCommand().add(fileName);
    commit(repositoryClient, username, "added " + fileName);
  }

  static Changeset commit(RepositoryClient repositoryClient, String username, String message) throws IOException {
    Changeset changeset = repositoryClient.getCommitCommand().commit(new Person(username, username + "@scm-manager.org"), message);
    if (repositoryClient.isCommandSupported(ClientCommand.PUSH)) {
      repositoryClient.getPushCommand().push();
    }
    return changeset;
  }
}
