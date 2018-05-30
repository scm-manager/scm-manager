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

public class LinkMapBuilderTest {

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
    LinkMapBuilder builder = new LinkMapBuilder(uriInfo, Main.class);
    builder
      .add("link")
      .method("sub")
      .parameters("param_x");

    URI actual = builder.getLinkMap().get("link").getHref();
    assertEquals("http://example.com/base/main/param_x", actual.toString());
  }

  @Test
  public void shouldBuildPathOverSubResources() {
    LinkMapBuilder builder = new LinkMapBuilder(uriInfo, Main.class, Sub.class);
    builder
      .add("link")
      .method("sub")
      .parameters("param_x")
      .method("x")
      .parameters("param_y", "param_z");

    URI actual = builder.getLinkMap().get("link").getHref();
    assertEquals("http://example.com/base/main/param_x/sub/param_y/param_z", actual.toString());
  }

  @Test
  public void shouldBuildPathWithoutParameters() {
    LinkMapBuilder builder = new LinkMapBuilder(uriInfo, NoParam.class);
    builder
      .add("link")
      .method("get")
      .parameters();

    URI actual = builder.getLinkMap().get("link").getHref();
    assertEquals("http://example.com/base", actual.toString());
  }

  @Test(expected = IllegalStateException.class)
  public void shouldFailForTooManyMethods() {
    LinkMapBuilder builder = new LinkMapBuilder(uriInfo, Main.class);
    builder
      .add("link")
      .method("sub")
      .parameters("param_x")
      .method("x");
  }

  @Before
  public void setBaseUri() throws URISyntaxException {
    when(uriInfo.getBaseUri()).thenReturn(new URI("http://example.com/"));
  }
}
