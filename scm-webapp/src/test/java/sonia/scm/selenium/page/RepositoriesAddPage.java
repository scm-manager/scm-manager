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

import com.google.common.base.MoreObjects;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.WebDriverWait;
import sonia.scm.repository.Repository;

import java.util.List;

/**
 * Page object for scm-manager's repository creation page.
 *
 * @author Sebastian Sdorra
 */
public class RepositoriesAddPage extends BasePage<RepositoriesAddPage> {
  
  @FindBy(css = "input[name=name]")
  private WebElement nameInput;
  
  @FindBy(css = "input[name=contact]")
  private WebElement contactInput;
  
  @FindBy(css = "#x-form-el-repositoryType img")
  private WebElement typeInput;
  
  @FindBy(css = "textarea[name=description]")
  private WebElement descriptionInput;
  
  @FindBy(css = "div.x-panel-btns button:nth-of-type(1)")
  private WebElement okButton;
  
  private final RepositoriesPage repositoriesPage;
  
  /**
   * Constructs a new page. This constructor should only be called from {@link Pages}.
   * 
   * @param driver selenium test driver
   * @param repositoriesPage repositories page object
   */
  RepositoriesAddPage(WebDriver driver, RepositoriesPage repositoriesPage) {
    super(driver);
    this.repositoriesPage = repositoriesPage;
  }
  
  @Override
  protected RepositoriesAddPage self() {
    return this;
  }
  
  /**
   * Creates a new {@link Repository}.
   * 
   * @param repository repository for creation
   * 
   * @return repositories overview page
   */
  public RepositoriesPage add(Repository repository) {
    nameInput.sendKeys(repository.getName());
    
    selectType(repository.getType());
    
    contactInput.sendKeys(repository.getContact());
    descriptionInput.sendKeys(repository.getDescription());
    
    waitToBeClickable(okButton).click();
    
    return repositoriesPage;
  }
    
  private void selectType(String type) {
    typeInput.click();
    
    String displayName = findDisplayName(type);
    
    WebDriverWait wait = new WebDriverWait(driver, 1);
    List<WebElement> elements = waitForAll(By.className("x-combo-list-item"));
    WebElement typeElement = null;
    for (WebElement te : elements){
      if (te.getText().trim().equalsIgnoreCase(displayName)){
        typeElement = te;
        break;
      }
    }
    
    if (typeElement == null){
      throw new NotFoundException("could not find type element with type " + displayName);
    }
    
    typeElement.click();
  }
  
  private String findDisplayName(String type) {
    String displayName = null; 
    if (driver instanceof JavascriptExecutor) {
      // TODO seams not to work
      String script = "Sonia.repository.getTypeByName('" + type + "').displayName;";
      displayName = (String) ((JavascriptExecutor)driver).executeScript(script);
    }    
    return MoreObjects.firstNonNull(displayName, type);
  }
  
}
