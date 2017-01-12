/**
 * Copyright (c) 2014, Sebastian Sdorra
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
package sonia.scm.selenium.page;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Page object for scm-manager's main page.
 * 
 * @author Sebastian Sdorra
 */
public class MainPage extends BasePage<MainPage> {
  
  @FindBy(css = "#navLogout a")
  private WebElement logoutLink;

  @FindBy(linkText = "Repositories")
  private WebElement repositoriesLink;
  
  @FindBy(css = "#scm-userinfo-tip")
  private WebElement userInfoTip;
  
  /**
   * Constructs a new page. This constructor should only be called from {@link Pages}.
   * 
   * @param driver selenium test driver
   */
  MainPage(WebDriver driver) {
    super(driver);
  }
  
  @Override
  protected MainPage self() {
    return this;
  }
  
  /**
   * Returns the name of the current authenticated user from the user info tip.
   * 
   * @return name of the current authenticated user
   */
  public String getUserInfo(){
    return userInfoTip.getText();
  }
  
  /**
   * Navigates to the repositories page and returns the page object for this page.
   * 
   * @return page object for repositories page
   */
  public RepositoriesPage repositories(){
    repositoriesLink.click();
    return Pages.get(driver, RepositoriesPage.class);
  }
  
  /**
   * Logs the current user out.
   * 
   * @return page object for the login
   */
  public LoginPage logout(){
    waitToBeClickable(logoutLink).click();
    return Pages.get(driver, LoginPage.class);
  }
}
