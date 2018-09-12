package sonia.scm.api.v2.resources;

import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ScmPathInfoStoreTest {

  @Test
  public void shouldReturnSetInfo() {
    URI someUri = URI.create("/anything");

    ScmPathInfo uriInfo = mock(ScmPathInfo.class);
    ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();

    when(uriInfo.getApiRestUri()).thenReturn(someUri);

    scmPathInfoStore.set(uriInfo);

    assertSame(someUri, scmPathInfoStore.get().getApiRestUri());
  }

  @Test(expected = IllegalStateException.class)
  public void shouldFailIfSetTwice() {
    ScmPathInfo uriInfo = mock(ScmPathInfo.class);
    ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();

    scmPathInfoStore.set(uriInfo);
    scmPathInfoStore.set(uriInfo);
  }
}
