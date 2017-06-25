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
