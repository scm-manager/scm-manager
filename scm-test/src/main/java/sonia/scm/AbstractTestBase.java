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

package sonia.scm;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.UnavailableSecurityManagerException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.LifecycleUtils;
import org.apache.shiro.util.ThreadState;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import sonia.scm.repository.InitialRepositoryLocationResolver;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.util.IOUtil;
import sonia.scm.util.MockUtil;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

import static java.util.Collections.emptySet;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Sebastian Sdorra
 */
public class AbstractTestBase
{

  private static ThreadState subjectThreadState;

  protected SCMContextProvider contextProvider;

  private File tempDirectory;

  protected RepositoryLocationResolver repositoryLocationResolver;

  @BeforeEach
  @Before
  public void setUpTest() throws Exception
  {
    tempDirectory = new File(System.getProperty("java.io.tmpdir"),
      UUID.randomUUID().toString());
    assertTrue(tempDirectory.mkdirs());
    contextProvider = MockUtil.getSCMContextProvider(tempDirectory);
    InitialRepositoryLocationResolver initialRepoLocationResolver = new InitialRepositoryLocationResolver(emptySet());
    repositoryLocationResolver = new TempDirRepositoryLocationResolver(tempDirectory);
    postSetUp();
  }

  /**
   * Method description
   *
   */
  @AfterAll
  @AfterClass
  public static void tearDownShiro()
  {
    doClearSubject();

    try
    {
      org.apache.shiro.mgt.SecurityManager securityManager =
        getSecurityManager();

      LifecycleUtils.destroy(securityManager);
    }
    catch (UnavailableSecurityManagerException e)
    {

      // we don't care about this when cleaning up the test environment
      // (for example, maybe the subclass is a unit test and it didn't
      // need a SecurityManager instance because it was using only
      // mock Subject instances)
    }

    setSecurityManager(null);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  protected static org.apache.shiro.mgt.SecurityManager getSecurityManager()
  {
    return SecurityUtils.getSecurityManager();
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param securityManager
   */
  protected static void setSecurityManager(
    org.apache.shiro.mgt.SecurityManager securityManager)
  {
    SecurityUtils.setSecurityManager(securityManager);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  private static void doClearSubject()
  {
    if (subjectThreadState != null)
    {
      subjectThreadState.clear();
      subjectThreadState = null;
    }
  }

  /**
   * Method description
   *
   *
   * @throws Exception
   */
  @AfterEach
  @After
  public void tearDownTest() throws Exception
  {
    try
    {
      preTearDown();
      clearSubject();
    }
    finally
    {
      try {
        IOUtil.delete(tempDirectory);
      } catch (IOException e) {
        Logger.getGlobal().warning(String.format("deleting temp <%s> failed: %s", tempDirectory.getAbsolutePath(), e.getMessage()));
      }
    }
  }


  /**
   * Clears Shiro's thread state, ensuring the thread remains clean for
   * future test execution.
   */
  protected void clearSubject()
  {
    doClearSubject();
  }

  /**
   * Method description
   *
   *
   * @param subject
   *
   * @return
   */
  protected ThreadState createThreadState(Subject subject)
  {
    return new SubjectThreadState(subject);
  }

  /**
   * Method description
   *
   *
   * @throws Exception
   */
  protected void postSetUp() throws Exception {}

  /**
   * Method description
   *
   *
   * @throws Exception
   */
  protected void preTearDown() throws Exception {}

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  protected Subject getSubject()
  {
    return SecurityUtils.getSubject();
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Allows subclasses to set the currently executing {@link Subject} instance.
   *
   * @param subject the Subject instance
   */
  protected void setSubject(Subject subject)
  {
    clearSubject();
    subjectThreadState = createThreadState(subject);
    subjectThreadState.bind();
  }

  protected File getTempDirectory() {
    return tempDirectory;
  }
}
