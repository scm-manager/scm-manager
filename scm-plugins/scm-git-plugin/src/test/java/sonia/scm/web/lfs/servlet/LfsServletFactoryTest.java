package sonia.scm.web.lfs.servlet;

import org.junit.Test;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.store.BlobStoreFactory;

import javax.servlet.http.HttpServletRequest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.*;

/**
 * Created by omilke on 18.05.2017.
 */
public class LfsServletFactoryTest {

  @Test
  public void buildBaseUri() throws Exception {

    String repositoryName = "git-lfs-demo";

    String result = LfsServletFactory.buildBaseUri(new Repository("", "GIT", repositoryName), RequestWithUri(repositoryName, true));
    assertThat(result, is(equalTo("http://localhost:8081/scm/git/git-lfs-demo.git/info/lfs/objects/")));


    //result will be with dot-gix suffix, ide
    result = LfsServletFactory.buildBaseUri(new Repository("", "GIT", repositoryName), RequestWithUri(repositoryName, false));
    assertThat(result, is(equalTo("http://localhost:8081/scm/git/git-lfs-demo.git/info/lfs/objects/")));
  }

  @Test
  public void getBlobStore() throws Exception {

    BlobStoreFactory blobStoreFactoryMock = mock(BlobStoreFactory.class);

    //TODO #239:
    RepositoryTestData repositoryTestData;


    new LfsServletFactory(blobStoreFactoryMock).getBlobStore(new Repository("the-id", "GIT", "the-name"));

    //just make sure the right parameter is passed, as properly validating the return value is nearly impossible with the return value (and should not be
    // part of this test)
    verify(blobStoreFactoryMock).getBlobStore(matches("the-id-git-lfs"));

    //make sure there have been no further usages of the factory
    verifyNoMoreInteractions(blobStoreFactoryMock);
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
