package sonia.scm.web.lfs.servlet;

import org.junit.Test;
import sonia.scm.repository.Repository;

import javax.servlet.http.HttpServletRequest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by omilke on 18.05.2017.
 */
public class LfsServletFactoryTest {

  @Test
  public void buildBaseUri() {

    String repositoryNamespace = "space";
    String repositoryName = "git-lfs-demo";

    String result = LfsServletFactory.buildBaseUri(new Repository("", "GIT", repositoryNamespace, repositoryName), RequestWithUri(repositoryName, true));
    assertThat(result, is(equalTo("http://localhost:8081/scm/git/space/git-lfs-demo.git/info/lfs/objects/")));


    //result will be with dot-git suffix, ide
    result = LfsServletFactory.buildBaseUri(new Repository("", "GIT", repositoryNamespace, repositoryName), RequestWithUri(repositoryName, false));
    assertThat(result, is(equalTo("http://localhost:8081/scm/git/space/git-lfs-demo.git/info/lfs/objects/")));
  }

  private HttpServletRequest RequestWithUri(String repositoryName, boolean withDotGitSuffix) {

    HttpServletRequest mockedRequest = mock(HttpServletRequest.class);

    final String suffix;
    if (withDotGitSuffix) {
      suffix = ".git";
    } else {
      suffix = "";
    }

    //build from valid live request data
    when(mockedRequest.getRequestURL()).thenReturn(
      new StringBuffer(String.format("http://localhost:8081/scm/git/%s%s/info/lfs/objects/batch", repositoryName, suffix)));
    when(mockedRequest.getRequestURI()).thenReturn(String.format("/scm/git/%s%s/info/lfs/objects/batch", repositoryName, suffix));
    when(mockedRequest.getContextPath()).thenReturn("/scm");

    return mockedRequest;
  }


}
