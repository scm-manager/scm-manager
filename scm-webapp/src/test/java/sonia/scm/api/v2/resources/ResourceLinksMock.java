package sonia.scm.api.v2.resources;

import java.net.URI;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static sonia.scm.api.v2.resources.GlobalConfigResource.GLOBAL_CONFIG_PATH_V2;
import static sonia.scm.api.v2.resources.GroupRootResource.GROUPS_PATH_V2;
import static sonia.scm.api.v2.resources.UserRootResource.USERS_PATH_V2;

public class ResourceLinksMock {
  public static void initMock(ResourceLinks resourceLinks, URI baseUri) {
    when(resourceLinks.user().self(anyString())).thenAnswer(invocation -> baseUri + USERS_PATH_V2 + invocation.getArguments()[0]);
    when(resourceLinks.user().update(anyString())).thenAnswer(invocation -> baseUri + USERS_PATH_V2 + invocation.getArguments()[0]);
    when(resourceLinks.user().delete(anyString())).thenAnswer(invocation -> baseUri + USERS_PATH_V2 + invocation.getArguments()[0]);

    when(resourceLinks.userCollection().self()).thenAnswer(invocation -> baseUri + USERS_PATH_V2);
    when(resourceLinks.userCollection().create()).thenAnswer(invocation -> baseUri + USERS_PATH_V2);

    when(resourceLinks.group().self(anyString())).thenAnswer(invocation -> baseUri + GROUPS_PATH_V2 + invocation.getArguments()[0]);
    when(resourceLinks.group().update(anyString())).thenAnswer(invocation -> baseUri + GROUPS_PATH_V2 + invocation.getArguments()[0]);
    when(resourceLinks.group().delete(anyString())).thenAnswer(invocation -> baseUri + GROUPS_PATH_V2 + invocation.getArguments()[0]);

    when(resourceLinks.groupCollection().self()).thenAnswer(invocation -> baseUri + GROUPS_PATH_V2);
    when(resourceLinks.groupCollection().create()).thenAnswer(invocation -> baseUri + GROUPS_PATH_V2);

    when(resourceLinks.globalConfig().self()).thenAnswer(invocation -> baseUri + GLOBAL_CONFIG_PATH_V2);
    when(resourceLinks.globalConfig().update()).thenAnswer(invocation -> baseUri + GLOBAL_CONFIG_PATH_V2);
  }
}
