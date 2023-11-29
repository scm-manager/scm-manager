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
    
package sonia.scm.api.v2.resources;

import jakarta.ws.rs.Path;
import org.junit.Before;
import org.junit.Test;

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

  private ScmPathInfo uriInfo = mock(ScmPathInfo.class);

  @Test
  public void shouldBuildSimplePath() {
    LinkBuilder builder = new LinkBuilder(uriInfo, Main.class);

    URI actual = builder
      .method("sub")
      .parameters("param_x")
      .create();
    assertEquals("http://example.com/base/main/param_x", actual.toString());
  }

  @Test
  public void shouldBuildPathOverSubResources() {
    LinkBuilder builder = new LinkBuilder(uriInfo, Main.class, Sub.class);

    URI actual = builder
      .method("sub")
      .parameters("param_x")
      .method("x")
      .parameters("param_y", "param_z")
      .create();
    assertEquals("http://example.com/base/main/param_x/sub/param_y/param_z", actual.toString());
  }

  @Test
  public void shouldBuildPathWithoutParameters() {
    LinkBuilder builder = new LinkBuilder(uriInfo, NoParam.class);

    URI actual = builder
      .method("get")
      .parameters()
      .create();
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
    when(uriInfo.getApiRestUri()).thenReturn(new URI("http://example.com/"));
  }
}
