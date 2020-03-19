/**
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

package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Assume;
import sonia.scm.SCMContext;
import sonia.scm.TempDirRepositoryLocationResolver;
import sonia.scm.security.AccessToken;
import sonia.scm.store.InMemoryConfigurationStoreFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.file.Path;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public final class HgTestUtil
{

  /**
   * Constructs ...
   *
   */
  private HgTestUtil() {}

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param handler
   */
  public static void checkForSkip(HgRepositoryHandler handler)
  {

    // skip tests if hg not in path
    if (!handler.isConfigured())
    {
      System.out.println("WARNING could not find hg, skipping test");
      Assume.assumeTrue(false);
    }

    if (Boolean.getBoolean("sonia.scm.test.skip.hg"))
    {
      System.out.println("WARNING mercurial test are disabled");
      Assume.assumeTrue(false);
    }
  }

  /**
   * Method description
   *
   *
   * @param directory
   *
   * @return
   */
  public static HgRepositoryHandler createHandler(File directory)  {
    TempSCMContextProvider context =
      (TempSCMContextProvider) SCMContext.getContext();

    context.setBaseDirectory(directory);

    RepositoryDAO repoDao = mock(RepositoryDAO.class);

    RepositoryLocationResolver repositoryLocationResolver = new TempDirRepositoryLocationResolver(directory);
    HgRepositoryHandler handler =
      new HgRepositoryHandler(new InMemoryConfigurationStoreFactory(), new HgContextProvider(), repositoryLocationResolver, null, null);
    handler.init(context);

    return handler;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public static HgHookManager createHookManager()
  {
    HgHookManager hookManager = mock(HgHookManager.class);

    when(hookManager.getChallenge()).thenReturn("challenge");
    when(hookManager.createUrl()).thenReturn(
      "http://localhost:8081/scm/hook/hg/");
    when(hookManager.createUrl(any(HttpServletRequest.class))).thenReturn(
      "http://localhost:8081/scm/hook/hg/");
    AccessToken accessToken = mock(AccessToken.class);
    when(accessToken.compact()).thenReturn("");
    when(hookManager.getAccessToken()).thenReturn(accessToken);

    return hookManager;
  }
}
