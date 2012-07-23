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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Sebastian Sdorra
 */
public class RepositoryCRUDITCase extends SeleniumTestBase
{

  /**
   * Method description
   *
   */
  @After
  public void after()
  {
    logout();
  }

  /**
   * Method description
   *
   */
  @Test
  public void createRepository() throws InterruptedException
  {
    waitAndClick("#repositoryAddButton");
    waitForPresence("input[name=name]").sendKeys("scm");
    select("#x-form-el-repositoryType img").click();
    waitAndClick("div.x-combo-list-item:nth-of-type(2)");
    type("input[name=contact]", "scmadmin@scm-manager.org");
    type("textarea[name=description]", "SCM-Manager");
    waitAndClick("div.x-panel-btns button:nth-of-type(1)");

    String name =
      waitForPresence(
          "div.x-grid3-row-selected div.x-grid3-col-name").getText();

    assertEquals("scm", name);
    
    waitAndClick("#repoRmButton button");
    waitAndClick("div.x-window button:nth-of-type(1)");
  }

  /**
   * Method description
   *
   */
  @Before
  public void login()
  {
    login("scmadmin", "scmadmin");
  }
}
