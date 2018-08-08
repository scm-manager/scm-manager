/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */


package sonia.scm.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.google.common.base.Charsets;
import com.sun.jersey.api.client.UniformInterfaceException;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import sonia.scm.api.rest.ObjectMapperProvider;
import sonia.scm.api.v2.resources.RepositoryDto;
import sonia.scm.repository.PermissionType;
import sonia.scm.repository.Repository;
import sonia.scm.user.User;
import sonia.scm.user.UserTestData;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static sonia.scm.it.IntegrationTestUtil.BASE_URL;
import static sonia.scm.it.IntegrationTestUtil.REST_BASE_URL;
import static sonia.scm.it.IntegrationTestUtil.createAdminClient;
import static sonia.scm.it.IntegrationTestUtil.readJson;
import static sonia.scm.it.RepositoryITUtil.createRepository;
import static sonia.scm.it.RepositoryITUtil.deleteRepository;

/**
 * Integration tests for git lfs.
 *
 * @author Sebastian Sdorra
 */
public class GitLfsITCase {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private final ObjectMapper mapper = new ObjectMapper();

  private ScmClient adminClient;

  private RepositoryDto repository;

  public GitLfsITCase() {
    mapper.setAnnotationIntrospector(new JaxbAnnotationIntrospector(TypeFactory.defaultInstance()));
  }

  // lifecycle methods

  @Before
  public void setUpTestDependencies() {
    adminClient = createAdminClient();
    repository = createRepository(adminClient, readJson("repository-git.json"));
  }

  @After
  public void tearDownTestDependencies() {
    deleteRepository(adminClient, repository);
  }

  // tests

  @Test
  public void testLfsAPIWithAdminPermissions() throws IOException {
    uploadAndDownload(adminClient);
  }

  @Test
  @Ignore("permissions not yet implemented")
  public void testLfsAPIWithOwnerPermissions() throws IOException {
    uploadAndDownloadAsUser(PermissionType.OWNER);
  }

  private void uploadAndDownloadAsUser(PermissionType permissionType) throws IOException {
    User trillian = UserTestData.createTrillian();
    trillian.setPassword("secret123");
    createUser(trillian);

    try {
      // TODO enable when permissions are implemented in v2
//      repository.getPermissions().add(new Permission(trillian.getId(), permissionType));
//      modifyRepository(repository);

      ScmClient client = new ScmClient(trillian.getId(), "secret123");

      uploadAndDownload(client);
    } finally {
      removeUser(trillian);
    }
  }

  @Test
  @Ignore("permissions not yet implemented")
  public void testLfsAPIWithWritePermissions() throws IOException {
    uploadAndDownloadAsUser(PermissionType.WRITE);
  }

  private void createUser(User user) {
    adminClient.resource(REST_BASE_URL + "users.json").post(user);
  }

  private void modifyRepository(Repository repository) {
    adminClient.resource(REST_BASE_URL + "repositories/" + repository.getId() + ".json").put(repository);
  }

  private void removeUser(User user) {
    adminClient.resource(REST_BASE_URL + "users/" + user.getId() + ".json").delete();
  }

  @Test
  @Ignore("permissions not yet implemented")
  public void testLfsAPIWithoutWritePermissions() throws IOException {
    User trillian = UserTestData.createTrillian();
    trillian.setPassword("secret123");
    createUser(trillian);

    expectedException.expect(UniformInterfaceException.class);
    expectedException.expectMessage(Matchers.containsString("403"));


    try {
      // TODO enable when permissions are implemented in v2
//      repository.getPermissions().add(new Permission(trillian.getId(), PermissionType.READ));
//      modifyRepository(repository);

      ScmClient client = new ScmClient(trillian.getId(), "secret123");
      uploadAndDownload(client);
    } finally {
      removeUser(trillian);
    }
  }

  @Test
  @Ignore("permissions not yet implemented")
  public void testLfsDownloadWithReadPermissions() throws IOException {
    User trillian = UserTestData.createTrillian();
    trillian.setPassword("secret123");
    createUser(trillian);


    try {
      // TODO enable when permissions are implemented in v2
//      repository.getPermissions().add(new Permission(trillian.getId(), PermissionType.READ));
//      modifyRepository(repository);

      // upload data as admin
      String data = UUID.randomUUID().toString();
      byte[] dataAsBytes = data.getBytes(Charsets.UTF_8);
      LfsObject lfsObject = upload(adminClient, dataAsBytes);

      ScmClient client = new ScmClient(trillian.getId(), "secret123");

      // download as user
      byte[] downloadedData = download(client, lfsObject);

      // assert both are equal
      assertArrayEquals(dataAsBytes, downloadedData);
    } finally {
      removeUser(trillian);
    }
  }

  // lfs api

  private void uploadAndDownload(ScmClient client) throws IOException {
    String data = UUID.randomUUID().toString();
    byte[] dataAsBytes = data.getBytes(Charsets.UTF_8);
    LfsObject lfsObject = upload(client, dataAsBytes);
    byte[] downloadedData = download(client, lfsObject);
    assertArrayEquals(dataAsBytes, downloadedData);
  }

  private LfsObject upload(ScmClient client, byte[] data) throws IOException {
    LfsObject lfsObject = createLfsObject(data);
    LfsRequestBody request = LfsRequestBody.createUploadRequest(lfsObject);
    LfsResponseBody response = request(client, request);

    String uploadURL = response.objects[0].actions.upload.href;
    client.resource(uploadURL).put(data);

    return lfsObject;
  }

  private LfsResponseBody request(ScmClient client, LfsRequestBody request) throws IOException {
    String batchUrl = createBatchUrl();
    String requestAsString = mapper.writeValueAsString(request);

    String json = client
      .resource(batchUrl)
      .accept("application/vnd.git-lfs+json")
      .header("Content-Type", "application/vnd.git-lfs+json")
      .post(String.class, requestAsString);
    return new ObjectMapperProvider().get().readValue(json, LfsResponseBody.class);
  }

  private String createBatchUrl() {
    String url = BASE_URL + "git/" + repository.getNamespace() + "/" + repository.getName();
    return url + "/info/lfs/objects/batch";
  }

  private byte[] download(ScmClient client, LfsObject lfsObject) throws IOException {
    LfsRequestBody request = LfsRequestBody.createDownloadRequest(lfsObject);
    LfsResponseBody response = request(client, request);

    String downloadUrl = response.objects[0].actions.download.href;
    return client.resource(downloadUrl).get(byte[].class);
  }

  private LfsObject createLfsObject(byte[] data) {
    Sha256Hash hash = new Sha256Hash(data);
    String oid = hash.toHex();
    return new LfsObject(oid, data.length);
  }

  // LFS DTO objects

  @XmlRootElement
  @XmlAccessorType(XmlAccessType.FIELD)
  private static class LfsRequestBody {

    private String operation;
    private String[] transfers = new String[]{ "basic" };
    private LfsObject[] objects;

    public LfsRequestBody() {
    }

    private LfsRequestBody(String operation, LfsObject[] objects) {
      this.operation = operation;
      this.objects = objects;
    }

    public static LfsRequestBody createUploadRequest(LfsObject object) {
      return new LfsRequestBody("upload", new LfsObject[]{object});
    }

    public static LfsRequestBody createDownloadRequest(LfsObject object) {
      return new LfsRequestBody("download", new LfsObject[]{object});
    }

  }

  @XmlRootElement
  @XmlAccessorType(XmlAccessType.FIELD)
  private static class LfsResponseBody {

    private LfsObject[] objects;

    public LfsResponseBody() {
    }

    public LfsResponseBody(LfsObject[] objects) {
      this.objects = objects;
    }
  }

  @XmlRootElement
  @XmlAccessorType(XmlAccessType.FIELD)
  private static class LfsObject {

    private String oid;
    private long size;
    private LfsActions actions;

    public LfsObject() {
    }

    public LfsObject(String oid, long size) {
      this.oid = oid;
      this.size = size;
    }

    public LfsObject(String oid, long size, LfsActions actions) {
      this.oid = oid;
      this.size = size;
      this.actions = actions;
    }

  }

  @XmlRootElement
  @XmlAccessorType(XmlAccessType.FIELD)
  private static class LfsActions {

    private LfsAction upload;
    private LfsAction download;

    public LfsActions() {
    }
  }

  @XmlRootElement
  @XmlAccessorType(XmlAccessType.FIELD)
  private static class LfsAction {

    private String href;

    public LfsAction() {
    }

    public LfsAction(String href) {
      this.href = href;
    }

  }

}
