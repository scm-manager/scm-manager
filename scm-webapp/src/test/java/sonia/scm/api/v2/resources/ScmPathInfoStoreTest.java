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
