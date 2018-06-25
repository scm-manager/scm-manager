package sonia.scm.api.v2.resources;

import org.junit.Test;

import javax.ws.rs.core.UriInfo;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

public class UriInfoStoreTest {

  @Test
  public void shouldReturnSetInfo() {
    UriInfo uriInfo = mock(UriInfo.class);
    UriInfoStore uriInfoStore = new UriInfoStore();

    uriInfoStore.set(uriInfo);

    assertSame(uriInfo, uriInfoStore.get());
  }

  @Test(expected = IllegalStateException.class)
  public void shouldFailIfSetTwice() {
    UriInfo uriInfo = mock(UriInfo.class);
    UriInfoStore uriInfoStore = new UriInfoStore();

    uriInfoStore.set(uriInfo);
    uriInfoStore.set(uriInfo);
  }
}
