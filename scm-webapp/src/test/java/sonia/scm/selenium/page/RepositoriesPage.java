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

import java.util.List;
import java.util.Locale;
import org.openqa.selenium.By;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import sonia.scm.repository.Repository;

/**
 * Page object for scm-manager's repositories overview page.
 *
 * @author Sebastian Sdorra
 */
public class RepositoriesPage extends BasePage<RepositoriesPage> {
  
  @FindBy(id = "repositoryAddButton")
  private WebElement addButton;
  
  /**
   * Constructs a new page. This constructor should only be called from {@link Pages}.
   * 
   * @param driver selenium test driver
   */
  RepositoriesPage(WebDriver driver) {
    super(driver);
  }
  
  @Override
  protected RepositoriesPage self() {
    return this;
  }

  /**
   * Creates a new {@link Repository}.
   * 
   * @param repository repository for creation
   * 
   * @return {@link this}
   */  
  public RepositoriesPage add(Repository repository){
    addButton.click();
    RepositoriesAddPage addPage = Pages.get(driver, RepositoriesAddPage.class, this);
    return addPage.add(repository);
  }
  
  /**
   * Selects the repository with the given name and returns the detail page object for the selected repository.
   * 
   * @param repositoryName name of the repository
   * 
   * @return page object for selected repository
   */
  public RepositoryPage select(String repositoryName){
    WebElement repositoryNameColumn = null;
    
    List<WebElement> elements = waitForAll(By.className("x-grid3-col-name"));
    for (WebElement element : elements){
      if (element.getText().trim().toLowerCase(Locale.ENGLISH).equals(repositoryName)){
        repositoryNameColumn = element;
        break;
      }
    }
    
    if ( repositoryNameColumn == null ) {
      throw new NotFoundException("could not find repository " + repositoryName);
    }
    
    return Pages.get(driver, RepositoryPage.class, this);
  }
}
