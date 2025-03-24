/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm;


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
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.util.IOUtil;
import sonia.scm.util.MockUtil;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.Assert.assertTrue;


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
    repositoryLocationResolver = new TempDirRepositoryLocationResolver(tempDirectory);
    postSetUp();
  }

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


  
  protected static org.apache.shiro.mgt.SecurityManager getSecurityManager()
  {
    return SecurityUtils.getSecurityManager();
  }



  protected static void setSecurityManager(
    org.apache.shiro.mgt.SecurityManager securityManager)
  {
    SecurityUtils.setSecurityManager(securityManager);
  }


   private static void doClearSubject()
  {
    if (subjectThreadState != null)
    {
      subjectThreadState.clear();
      subjectThreadState = null;
    }
  }


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


  protected ThreadState createThreadState(Subject subject)
  {
    return new SubjectThreadState(subject);
  }


  protected void postSetUp() throws Exception {}


  protected void preTearDown() throws Exception {}


  
  protected Subject getSubject()
  {
    return SecurityUtils.getSubject();
  }


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
