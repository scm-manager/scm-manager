package sonia.scm.it;

import com.sun.jersey.api.client.ClientResponse;
import sonia.scm.user.User;
import sonia.scm.web.VndMediaType;

import static sonia.scm.it.IntegrationTestUtil.post;

public class UserITUtil {
  public static ClientResponse postUser(ScmClient client, User user) {
    return post(client, "users", VndMediaType.USER, user);
  }
}
