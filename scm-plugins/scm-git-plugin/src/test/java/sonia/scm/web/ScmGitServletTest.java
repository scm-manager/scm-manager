package sonia.scm.web;

import org.junit.Test;

import static org.eclipse.jgit.lfs.lib.Constants.CONTENT_TYPE_GIT_LFS_JSON;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by omilke on 11.05.2017.
 */
public class ScmGitServletTest {

  @Test
  public void isContentTypeMatches() throws Exception {

    assertThat(ScmGitServlet.isLfsContentHeaderField("application/vnd.git-lfs+json", CONTENT_TYPE_GIT_LFS_JSON), is(true));
    assertThat(ScmGitServlet.isLfsContentHeaderField("application/vnd.git-lfs+json;", CONTENT_TYPE_GIT_LFS_JSON), is(true));
    assertThat(ScmGitServlet.isLfsContentHeaderField("application/vnd.git-lfs+json; charset=utf-8", CONTENT_TYPE_GIT_LFS_JSON), is(true));

    assertThat(ScmGitServlet.isLfsContentHeaderField("application/vnd.git-lfs-json;", CONTENT_TYPE_GIT_LFS_JSON), is(false));
    assertThat(ScmGitServlet.isLfsContentHeaderField("", CONTENT_TYPE_GIT_LFS_JSON), is(false));
    assertThat(ScmGitServlet.isLfsContentHeaderField(null, CONTENT_TYPE_GIT_LFS_JSON), is(false));
  }

}
