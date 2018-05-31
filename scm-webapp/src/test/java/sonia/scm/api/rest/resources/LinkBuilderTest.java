package sonia.scm.api.rest.resources;

import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.Path;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LinkBuilderTest {

  @Path("base")
  public static class Main {
    @Path("main/{x}")
    public Sub sub() {
      return null;
    }
  }

  public static class Sub {
    @Path("sub/{y}/{z}")
    public Object x() {
      return null;
    }
  }

  @Path("base")
  public static class NoParam {
    @Path("")
    public Object get() {
      return null;
    }
  }

  private UriInfo uriInfo = mock(UriInfo.class);

  @Test
  public void shouldBuildSimplePath() {
    LinkBuilder builder = new LinkBuilder(uriInfo, Main.class);

    URI actual = builder
      .method("sub")
      .parameters("param_x")
      .create()
      .getHref();
    assertEquals("http://example.com/base/main/param_x", actual.toString());
  }

  @Test
  public void shouldBuildPathOverSubResources() {
    LinkBuilder builder = new LinkBuilder(uriInfo, Main.class, Sub.class);

    URI actual =     builder
      .method("sub")
      .parameters("param_x")
      .method("x")
      .parameters("param_y", "param_z")
      .create()
      .getHref();
    assertEquals("http://example.com/base/main/param_x/sub/param_y/param_z", actual.toString());
  }

  @Test
  public void shouldBuildPathWithoutParameters() {
    LinkBuilder builder = new LinkBuilder(uriInfo, NoParam.class);

    URI actual = builder
      .method("get")
      .parameters()
      .create()
      .getHref();
    assertEquals("http://example.com/base", actual.toString());
  }

  @Test(expected = IllegalStateException.class)
  public void shouldFailForTooManyMethods() {
    LinkBuilder builder = new LinkBuilder(uriInfo, Main.class);
    builder
      .method("sub")
      .parameters("param_x")
      .method("x");
  }

  @Test(expected = IllegalStateException.class)
  public void shouldFailForTooFewMethods() {
    LinkBuilder builder = new LinkBuilder(uriInfo, Main.class, Sub.class);
    builder
      .method("sub")
      .parameters("param_x")
      .create();
  }

  @Before
  public void setBaseUri() throws URISyntaxException {
    when(uriInfo.getBaseUri()).thenReturn(new URI("http://example.com/"));
  }
}
