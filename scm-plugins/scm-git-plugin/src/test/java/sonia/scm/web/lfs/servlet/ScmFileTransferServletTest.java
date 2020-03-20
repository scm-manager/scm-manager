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
    
package sonia.scm.web.lfs.servlet;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;

/**
 * Created by omilke on 16.05.2017.
 */
public class ScmFileTransferServletTest {

  @Test
  public void hasObjectId() throws Exception {

    String SAMPLE_OBJECT_ID = "8fcebeb5698230685f92028e560f8f1683ebc15ec82a620ffad5c12a3c19bdec";

    String path = "/git-lfs-demo.git/info/lfs/objects/" + SAMPLE_OBJECT_ID;
    assertThat(ScmFileTransferServlet.objectIdFromPath(path), is(equalTo(SAMPLE_OBJECT_ID)));

    path = "/" + SAMPLE_OBJECT_ID;
    assertThat(ScmFileTransferServlet.objectIdFromPath(path), is(equalTo(SAMPLE_OBJECT_ID)));

    path = SAMPLE_OBJECT_ID;
    assertThat(ScmFileTransferServlet.objectIdFromPath(path), is(equalTo(SAMPLE_OBJECT_ID)));

    String nonObjectId = "this-ist-last-to-found";
    path = "/git-lfs-demo.git/info/lfs/objects/" + nonObjectId;
    assertThat(ScmFileTransferServlet.objectIdFromPath(path), is(nullValue()));

    nonObjectId = SAMPLE_OBJECT_ID.substring(1);
    path = "/git-lfs-demo.git/info/lfs/objects/" + nonObjectId;
    assertThat(ScmFileTransferServlet.objectIdFromPath(path), is(nullValue()));

    nonObjectId = SAMPLE_OBJECT_ID + "X";
    path = "/git-lfs-demo.git/info/lfs/objects/" + nonObjectId;
    assertThat(ScmFileTransferServlet.objectIdFromPath(path), is(nullValue()));

  }
}
