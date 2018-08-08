package sonia.scm.it;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import java.util.Base64;

import static sonia.scm.it.IntegrationTestUtil.createClient;

public class ScmClient {
  private final String user;
  private final String password;

  private final Client client;

  public static ScmClient anonymous() {
    return new ScmClient(null, null);
  }

  public ScmClient(String user, String password) {
    this.user = user;
    this.password = password;
    this.client = createClient();
  }

  public WebResource.Builder resource(String url) {
    if (user == null) {
      return client.resource(url).getRequestBuilder();
    } else {
      return client.resource(url).header("Authorization", createAuthHeaderValue());
    }
  }

  public String createAuthHeaderValue() {
    return "Basic " + Base64.getEncoder().encodeToString((user +":"+ password).getBytes());
  }
}
