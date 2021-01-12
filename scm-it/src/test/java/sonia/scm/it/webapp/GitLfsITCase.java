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

package sonia.scm.it.webapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.google.common.base.Charsets;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import sonia.scm.api.rest.ObjectMapperProvider;
import sonia.scm.api.v2.resources.RepositoryDto;
import sonia.scm.api.v2.resources.UserDto;
import sonia.scm.api.v2.resources.UserToUserDtoMapperImpl;
import sonia.scm.user.User;
import sonia.scm.user.UserTestData;
import sonia.scm.util.HttpUtil;
import sonia.scm.web.VndMediaType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static sonia.scm.it.webapp.IntegrationTestUtil.BASE_URL;
import static sonia.scm.it.webapp.IntegrationTestUtil.REST_BASE_URL;
import static sonia.scm.it.webapp.IntegrationTestUtil.createAdminClient;
import static sonia.scm.it.webapp.IntegrationTestUtil.createResource;
import static sonia.scm.it.webapp.IntegrationTestUtil.readJson;
import static sonia.scm.it.webapp.RepositoryITUtil.createRepository;
import static sonia.scm.it.webapp.RepositoryITUtil.deleteRepository;

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
  public void testLfsAPIWithOwnerPermissions() throws IOException {
    User trillian = UserTestData.createTrillian();
    trillian.setPassword("secret123");
    createUser(trillian);

    try {
      String permissionsUrl = repository.getLinks().getLinkBy("permissions").get().getHref();
      IntegrationTestUtil.createResource(adminClient, URI.create(permissionsUrl))
        .accept("*/*")
        .type(VndMediaType.REPOSITORY_PERMISSION)
        .post(ClientResponse.class, "{\"name\": \""+ trillian.getId() +"\", \"verbs\":[\"*\"]}");

      ScmClient client = new ScmClient(trillian.getId(), "secret123");

      uploadAndDownload(client);
    } finally {
      removeUser(trillian);
    }
  }

  private void createUser(User user) {
    UserDto dto = new UserDto();
    dto.setName(user.getName());
    dto.setMail(user.getMail());
    dto.setDisplayName(user.getDisplayName());
    dto.setType(user.getType());
    dto.setActive(user.isActive());
    dto.setPassword(user.getPassword());
    createResource(adminClient, "users")
      .accept("*/*")
      .type(VndMediaType.USER)
      .post(ClientResponse.class, dto);
  }

  private void removeUser(User user) {
    adminClient.resource(REST_BASE_URL + "users/" + user.getId()).delete();
  }

  @Test
  public void testLfsAPIWithoutWritePermissions() throws IOException {
    User trillian = UserTestData.createTrillian();
    trillian.setPassword("secret123");
    createUser(trillian);

    expectedException.expect(UniformInterfaceException.class);
    expectedException.expectMessage(Matchers.containsString("403"));


    try {
      String permissionsUrl = repository.getLinks().getLinkBy("permissions").get().getHref();
      IntegrationTestUtil.createResource(adminClient, URI.create(permissionsUrl))
        .accept("*/*")
        .type(VndMediaType.REPOSITORY_PERMISSION)
        .post(ClientResponse.class, "{\"name\": \""+ trillian.getId() +"\", \"verbs\":[\"read\"]}");

      ScmClient client = new ScmClient(trillian.getId(), "secret123");
      uploadAndDownload(client);
    } finally {
      removeUser(trillian);
    }
  }

  @Test
  public void testLfsDownloadWithReadPermissions() throws IOException {
    User trillian = UserTestData.createTrillian();
    trillian.setPassword("secret123");
    createUser(trillian);


    try {
      String permissionsUrl = repository.getLinks().getLinkBy("permissions").get().getHref();
      IntegrationTestUtil.createResource(adminClient, URI.create(permissionsUrl))
        .accept("*/*")
        .type(VndMediaType.REPOSITORY_PERMISSION)
        .post(ClientResponse.class, "{\"name\": \""+ trillian.getId() +"\", \"verbs\":[\"read\",\"pull\"]}");

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
    client.resource(uploadURL).header(HttpUtil.HEADER_USERAGENT, "git-lfs/z").put(data);

    return lfsObject;
  }

  private LfsResponseBody request(ScmClient client, LfsRequestBody request) throws IOException {
    String batchUrl = createBatchUrl();
    String requestAsString = mapper.writeValueAsString(request);

    String json = client
      .resource(batchUrl)
      .accept("application/vnd.git-lfs+json")
      .header(HttpUtil.HEADER_USERAGENT, "git-lfs/z")
      .header("Content-Type", "application/vnd.git-lfs+json")
      .post(String.class, requestAsString);
    return new ObjectMapperProvider().get().readValue(json, LfsResponseBody.class);
  }

  private String createBatchUrl() {
    return String.format("%srepo/%s/%s/info/lfs/objects/batch", BASE_URL, repository.getNamespace(), repository.getName());
  }

  private byte[] download(ScmClient client, LfsObject lfsObject) throws IOException {
    LfsRequestBody request = LfsRequestBody.createDownloadRequest(lfsObject);
    LfsResponseBody response = request(client, request);

    String downloadUrl = response.objects[0].actions.download.href;
    return client.resource(downloadUrl).header(HttpUtil.HEADER_USERAGENT, "git-lfs/z").get(byte[].class);
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
