/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */


package sonia.scm.selenium;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.selenium.page.Pages;
import sonia.scm.selenium.page.MainPage;
import sonia.scm.selenium.page.LoginPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sonia.scm.repository.Repository;

/**
 * Repository related selenium integration tests.
 * 
 * @author Sebastian Sdorra
 */
public class RepositoriesITCase extends SeleniumITCaseBase {

  private MainPage main;
  
  /**
   * Authenticates admin user, before each test.
   */
  @Before
  public void login() {
    main = Pages.get(driver, LoginPage.class)
                .login("scmadmin", "scmadmin");
  }

  /**
   * Creates, select and removes a repository. 
   */
  @Test
  public void createRepository() {
    Repository repository = new Repository();
    repository.setName("scm");
    repository.setType("git");
    repository.setContact("scmadmin@scm-manager.org");
    repository.setDescription("SCM-Manager");
    
    main.repositories()
        .add(repository)
        .select(repository.getName())
        .remove();
  }
  
  /**
   * Logs the user out, after each test.
   */
  @After
  public void logout() {
    main.logout();
  }
}
