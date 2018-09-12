package sonia.scm.api.v2.resources;

import org.junit.Test;

import javax.ws.rs.core.UriInfo;
import java.net.URI;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ScmPathInfoStoreTest {

  @Test
  public void shouldReturnSetInfo() {
    URI someUri = URI.create("/anything");

    UriInfo uriInfo = mock(UriInfo.class);
    ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();

    when(uriInfo.getBaseUri()).thenReturn(someUri);

    scmPathInfoStore.setFromRestRequest(uriInfo);

    assertSame(someUri, scmPathInfoStore.get().getApiRestUri());
  }

  @Test(expected = IllegalStateException.class)
  public void shouldFailIfSetTwice() {
    UriInfo uriInfo = mock(UriInfo.class);
    ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();

    scmPathInfoStore.setFromRestRequest(uriInfo);
    scmPathInfoStore.setFromRestRequest(uriInfo);
  }
}
